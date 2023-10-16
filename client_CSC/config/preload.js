const { contextBridge, ipcRenderer, ipcMain, dialog } = require("electron");
const fs = require("fs");
const http = require("http");
const axios = require("axios").default;
const path = require("path");
const refPath = require("path");
const homedir = require("os").homedir();
const apphome = path.join(homedir, ".codegen");
const appDriverHome = path.join(apphome, "drivers");
const downloadHomeDir = path.join(homedir, ".codegen_repository");
const platform = process.platform;
let crtDirName = __dirname;
// const unzip = require("unzip");
const decompress = require("decompress");
const decompressTargz = require("decompress-targz");
const _ = require("lodash");
const refmachine = require("node-machine-id");
const cmd = require("node-cmd");
const preCache = {
  machineId: null,
};
const ruid = ipcRenderer.sendSync("ruid");
const puid = ipcRenderer.sendSync("puid");
var tcpPortUsed = require("tcp-port-used");

function readDirSync(path, myfilearr) {
  var pa = fs.readdirSync(path);
  pa.forEach(function (ele, index) {
    var info = fs.statSync(path + "/" + ele);
    if (info.isDirectory()) {
      readDirSync(path + "/" + ele, myfilearr);
    } else {
      myfilearr.push({
        name: ele,
        path: path,
        fullpath: refPath.join(path, ele),
      });
    }
  });
}

// let beginPort = 35000;

// var checkUtils = {
//   checkPort(runFunc, config) {
//     runFunc();
//     console.log("checking port", beginPort);
//     tcpPortUsed.check(beginPort, "127.0.0.1").then(
//       function (inUse) {
//         console.log("Port " + beginPort + " is using: " + inUse);
//         if (inUse) {
//           beginPort++;
//           checkUtils.checkPort(runFunc, config);
//         } else {
//           console.log("launch app");
//           runFunc(serverPortInfo.port);
//         }
//       },
//       function (err) {
//         console.error("Error on check:", err.message);
//       }
//     );
//   },
// };

const apiRef = {
  kill() {
    return new Promise((resolve, reject) => {
      apiRef.execCommand("pkill LightingBoot");
    });
  },
  getAvailablePort(beginPort) {
    return new Promise((resolve, reject) => {
      tcpPortUsed.check(beginPort, "127.0.0.1").then(
        function (inUse) {
          console.log("Port " + beginPort + " is using: " + inUse);
          resolve(inUse);
        },
        function (err) {
          reject(err);
        }
      );
    });
  },
  setPort(value) {
    apiRef.port = value;
    ipcRenderer.sendSync("chgport", value);
  },
  getPort() {
    return ipcRenderer.sendSync("port");
  },
  execCommand(cmdstr, callback) {
    const myref = cmd.run(cmdstr);
    myref.stdout.on("data", function (data) {
      if (callback.data) {
        callback.data(data);
      }
    });
    myref.stdout.on("close", (mydata) => {
      if (callback.close) {
        callback.close(mydata);
      }
    });
    myref.stderr.on("data", (errdata) => {
      if (callback.err) {
        callback.err(errdata);
      }
    });
  },
  puid,
  writeBootLog: function () {
    let bootJSONFile = path.join(
      apiRef.getAppHomePath(),
      "boot_ui_service.json"
    );
    if (fs.existsSync(bootJSONFile)) {
      fs.writeFileSync(bootJSONFile, JSON.stringify([{ puid: puid }]));
    }
  },
  getBootLog: function () {
    const bootJSONFile = path.join(
      apiRef.getAppHomePath(),
      "boot_ui_service.json"
    );
    if (!fs.existsSync(bootJSONFile)) {
      fs.writeFileSync(bootJSONFile, "[]");
    }
    const bootJSON = fs.readFileSync(bootJSONFile).toString();
    if (_.isEmpty(bootJSON)) {
      bootJSON = "[]";
    }
    return JSON.parse(bootJSON);
  },
  testFunc() {
    const myfilearr = [];
    console.log(apiRef.getAppDownloadPathByJoin("runtime"));
    readDirSync(apiRef.getAppDownloadPathByJoin("runtime"), myfilearr);
    return myfilearr;
  },
  getRunMainBackendTotalCmd(runport) {
    return apiRef.getRunBackendServerCmd(
      `com.denote.client.LightingBoot ${runport} prod`
    );
  },
  getRunBackendServerCmd(runClz) {
    const myfilearr = [];
    readDirSync(apiRef.getAppDownloadPathByJoin("runtime"), myfilearr);
    const allJarFilesInCore = [];
    readDirSync(apiRef.getAppDownloadPathByJoin("core"), allJarFilesInCore);
    const javapath = _.find(
      myfilearr,
      (x) => x.name == "java" || x.name == "java.exe"
    );
    let allJarFileStr = _.chain(myfilearr)
      .concat(allJarFilesInCore)
      .filter((x) => {
        if (x.name.endsWith("jar")) {
          return true;
        } else {
          return false;
        }
      })
      .map((x) => x.fullpath)
      .join(apiRef.clzSep)
      .value();

    return `${
      javapath.path + apiRef.sep + javapath.name
    } -cp ${allJarFileStr}:${path.join(
      __dirname,
      "infra",
      "run-client"
    )} ${runClz}`;
  },
  getMachineId: function () {
    if (_.isNil(preCache.machineId)) {
      preCache.machineId = refmachine.machineIdSync();
    }
    return preCache.machineId;
  },
  decompress(filepath, myParentDir) {
    filepath = filepath.trim();
    let isTargz = filepath.endsWith(".tar.gz");
    let parentDir = !_.isNil(myParentDir)
      ? myParentDir
      : path.dirname(filepath);
    if (!fs.existsSync(parentDir)) {
      fs.mkdirSync(parentDir, { recursive: true });
    }
    return new Promise((resolve, reject) => {
      decompress(
        filepath,
        parentDir,
        isTargz
          ? {
              plugins: [decompressTargz()],
            }
          : undefined
      )
        .then((arr) => {
          console.log("Files decompressed");
          if (_.isEmpty(arr)) {
            reject("cannot decompress the file, please check " + filepath);
          } else {
            resolve(parentDir);
          }
        })
        .catch((err) => {
          reject(err);
        });
    });
  },
  platform,
  getRepoJSON() {
    let basisRepositoryJSON = fs
      .readFileSync(path.join(__dirname, "infra", "basis-repository.json"))
      .toString();
    return JSON.parse(basisRepositoryJSON);
  },
  getClientJarFilePath() {
    return path.join(__dirname, "infra", "client-1.0-pg.jar");
  },
  testDownloadFile: () => {
    let file = "/Users/jerrylai/test/save_electron.tar.gz";
    if (fs.existsSync(file)) {
      fs.rmSync(file);
    }
    let url =
      "https://mirrors.tuna.tsinghua.edu.cn/AdoptOpenJDK/8/jre/x64/linux/OpenJDK8U-jre_x64_linux_hotspot_8u322b06.tar.gz";
    apiRef.downloadFile(url, file, {
      onProgress() {},
      onSuccess() {},
      onFail() {},
    });
  },
  sep: platform == "win32" ? "\\" : "/",
  clzSep: platform == "win32" ? ";" : ":",
  downloadJar(mvnPkg, funcConfig = {}) {
    let baseLink = apiRef.getMavenBase();
    let myfilename = mvnPkg.artifactId + "-" + mvnPkg.version + mvnPkg.type;
    let completJar =
      baseLink +
      mvnPkg.groupId.replaceAll(".", "/") +
      "/" +
      mvnPkg.artifactId +
      "/" +
      mvnPkg.version +
      "/" +
      myfilename;
    console.log("complete jar", completJar);
    if (_.isNil(global.downloadURLWork)) {
      global.downloadURLWork = {};
    }
    if (!_.isNil(global.downloadURLWork[completJar])) {
      global.downloadURLWork[completJar].cancel();
    }
    const saveFile = path.join(
      appDriverHome,
      mvnPkg.groupId,
      mvnPkg.artifactId,
      myfilename
    );
    if (funcConfig.refPath) {
      funcConfig.refPath({ saveFile });
    }
    let saveFileDoneFlag = saveFile + "_done";
    if (fs.existsSync(saveFileDoneFlag)) {
      funcConfig.onSuccess();
      return;
    }

    apiRef.downloadFile(completJar, saveFile, {
      onProgress(e) {
        funcConfig.onProgress(e);
      },
      onSuccess(e) {
        funcConfig.onSuccess(e);
        global.downloadURLWork[completJar] = null;
        fs.writeFileSync(saveFileDoneFlag, "ok");
      },
      onFail(e) {
        funcConfig.onFail(e);
        global.downloadURLWork[completJar] = null;
        if (fs.existsSync(saveFileDoneFlag)) {
          fs.rmSync(saveFileDoneFlag);
        }
      },
      ref(e) {
        global.downloadURLWork[completJar] = {
          cancel: () => {
            e.cancel("AUTO CANCEL");
          },
        };
      },
    });
  },
  getMavenBase() {
    const repoJSON = apiRef.getRepoJSON();
    const initJSON = apiRef.readInitJSON();
    return _.shuffle(repoJSON.core[initJSON.mirror])[0];
  },
  downloadFile: (url, filePath, funcConfig = {}) => {
    // _.defaultsDeep(funcConfig, {
    //   onProgress: () => {},
    //   onSuccess: () => {},
    //   onFail: () => {},
    // });
    console.log("receiving download action");
    const CancelToken = axios.CancelToken;
    const source = CancelToken.source();
    if (funcConfig && funcConfig.ref) {
      funcConfig.ref(source);
    }
    const latestObj = {};
    const ref = axios({
      method: "get",
      url: url,
      cancelToken: source.token,
      responseType: "blob",
      onDownloadProgress: (event) => {
        // console.log("onDownloadProgress", event);
        const e = event;
        funcConfig.onProgress({
          cancel() {
            source.cancel("Operation cancelled.");
          },
          loaded: (event.loaded / 1024).toFixed(0) + "M",
          total: (e.total / 1024).toFixed(0) + "M",
          loaded_num: event.loaded,
          total_num: e.total,
          done: event.loaded == e.total,
          rate: ((event.loaded / e.total) * 100).toFixed(2) + "%",
        });
      },
    })
      .then((response) => {
        response.data.arrayBuffer().then((buffer) => {
          fs.mkdirSync(path.dirname(filePath), { recursive: true });
          buffer = Buffer.from(buffer);
          let stream = fs.createWriteStream(filePath);
          stream.on("error", (err) => {
            funcConfig.onFail(err);
          });
          stream.write(buffer, function (err) {
            if (err) {
              funcConfig.onFail(err);
            } else {
              funcConfig.onSuccess();
            }
          });
        });
      })
      .catch((err) => {
        console.log(err);
        funcConfig.onFail(err);
      });
  },
  hasFile: (str) => {
    return fs.existsSync(str);
  },
  hasFileInHome(str) {
    str = path.join(apphome, str);
    return apiRef.hasFile(str);
  },
  getCrtDirPath(path) {
    return path.join(crtDirName, path);
  },

  isBrandNewAndNeedDownloadInfra() {
    return !apiRef.hasFile(path.join(apiRef.getAppDownloadPath(), "init.json"));
  },
  isLocalServerNotStartedUp() {
    if (apiRef.dev) {
      return false;
    }
    const bootItem = apiRef.getBootLog();
    const findItem = _.find(bootItem, (x) => x.puid == puid);
    return _.isNil(findItem);
  },
  saveInitJson(value) {
    fs.writeFileSync(
      path.join(apiRef.getAppDownloadPath(), "init.json"),
      value
    );
  },
  readInitJSON() {
    return JSON.parse(
      fs
        .readFileSync(
          path.join(apiRef.getAppDownloadPath(), "init.json"),
          "utf-8"
        )
        .toString()
    );
  },
  getAppHomePath() {
    return apphome;
  },
  getAppDownloadPath() {
    return downloadHomeDir;
  },
  getAppDownloadPathByJoin(mypath) {
    return path.join(downloadHomeDir, mypath);
  },
  deleteDir(path) {
    if (fs.existsSync(path)) {
      fs.rmdirSync(path, { recursive: true });
    }
  },
  rm(path) {
    fs.rmSync(path);
  },
  mkdirDir(path) {
    fs.mkdirSync(path, { recursive: true });
  },
  hasFileInDownloadHome(str) {
    str = path.join(downloadHomeDir, str);
    return apiRef.hasFile(str);
  },
  readFile(filePath) {
    return fs.readFileSync(filePath, { encoding: "UTF-8" }).toString();
  },
  writeFile(filepath, content) {
    return new Promise((resolve, reject) => {
      fs.writeFile(filepath, content, (err) => {
        if (err) {
          reject(err);
        } else {
          resolve();
        }
      });
    });
  },
  homedir,
  ruid: ruid,
  port: ipcRenderer.sendSync("port"),
  dev: fs.existsSync(path.join(apphome, ".dev")),
  send: (channel, data) => {
    ipcRenderer.send(channel, data);
  },
  sendSync: (channel, data) => {
    return ipcRenderer.sendSync(channel, data);
  },
  receive: (channel, func) => {
    // Deliberately strip event as it includes `sender`
    ipcRenderer.on(channel, (event, args) => {
      func(args);
    });
  },
  receiveOnce: (channel, func) => {
    // Deliberately strip event as it includes `sender`
    ipcRenderer.once(channel, (event, args) => {
      func(args);
    });
  },
};
contextBridge.exposeInMainWorld("api", apiRef);
contextBridge.exposeInMainWorld("ipc", apiRef);
contextBridge.exposeInMainWorld("preload", apiRef);

module.exports = apiRef;
