const { app, BrowserWindow, dialog } = require("electron");
const { ipcMain } = require("electron");
var tcpPortUsed = require("tcp-port-used");
function uuid() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
    .replace(/[xy]/g, function (c) {
      var r = (Math.random() * 16) | 0,
        v = c == "x" ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    })
    .replace(/-/gi, "");
}
var os = require("os");
var path = require("path");
var fs = require("fs");
var homedir = os.homedir();
const axios = require("axios").default;
var codeGenDir = path.join(homedir, ".codegen");
if (!fs.existsSync(codeGenDir)) {
  fs.mkdirSync(codeGenDir);
}
var viewkeyfile = path.join(codeGenDir, ".key4denote");
var isViewKeyFileExists = fs.existsSync(viewkeyfile);
var crtuuid = null;
var crt_puid = uuid();
if (!isViewKeyFileExists) {
  crtuuid = uuid();
  fs.writeFileSync(viewkeyfile, crtuuid);
} else {
  crtuuid = fs.readFileSync(viewkeyfile, "UTF-8");
}
const downloadHomeDir = path.join(homedir, ".codegen_repository");

const apphome = path.join(homedir, ".codegen");
var isDev = fs.existsSync(path.join(apphome, ".dev"));

var serverPortInfo = {
  port: isDev ? 18080 : -1,
};

ipcMain.on("ruid", (event, arg) => {
  event.returnValue = crtuuid;
});
ipcMain.on("puid", (event, arg) => {
  event.returnValue = crt_puid;
});

ipcMain.on("port", (event, arg) => {
  event.returnValue = serverPortInfo.port;
});

ipcMain.on("chgport", (event, arg) => {
  console.log("receive chg port", arg);
  serverPortInfo.port = arg;
  event.returnValue = arg;
});

ipcMain.on("select-dirs", (event, arg) => {
  dialog
    .showOpenDialog(mainWindow, {
      properties: ["openDirectory"],
    })
    .then((result) => {
      console.log("directories selected", result.filePaths);
      event.sender.send("get-dirs", result.filePaths);
    });
});

// ipcMain.on("download", (event, url, path) => {
// console.log("receiving download action", url, path);
// axios({
//   method: "get",
//   url: url,
//   responseType: "stream",
//   onDownloadProgress: (event) => {
//     console.log("onDownloadProgress", event);
//   },
// })
//   .then((response) => {
//     const writer = fs.createWriteStream(path);
//     response.data.pipe(writer);
//     writer.on("finish", () => {
//       console.log("finish writed");
//     });
//     writer.on("error", () => {
//       console.log("got error");
//     });
//   })
//   .catch((err) => {
//     console.log(err);
//   });
// });

var cui = {
  calcw(value) {
    let size = require("electron").screen.getPrimaryDisplay().workAreaSize;
    var width = parseInt(size.width);
    var height = parseInt(size.height);
    return parseInt((value / 1920) * width);
  },
  calch(value) {
    let size = require("electron").screen.getPrimaryDisplay().workAreaSize;
    var width = parseInt(size.width);
    var height = parseInt(size.height);
    return parseInt((value / 1080) * height);
  },
};

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;

function createWindow() {
  let w_size = require("electron").screen.getPrimaryDisplay().workAreaSize;
  var w_width = parseInt(w_size.width);
  var w_height = parseInt(w_size.height);
  var crtWin = createBasicWindow({
    // width: 1256,
    // height: 856,
    width: w_width,
    height: w_height,
    htmlfile: "./web/dist/index/index.html",
  });
  return crtWin;
}
function createBasicWindow({ width, height, htmlfile }) {
  // Create the browser window.
  // let filename = "images/b9.png";
  let filename = "images/b10.png";
  var mainWindow = new BrowserWindow({
    title: "CodeGen",
    // icon: './images/icon.ico',
    icon: path.join(__dirname, filename),
    // frame: false,
    // width: cui.calcw(width),
    // height: cui.calch(height),
    width: width,
    height: height,
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      nodeIntegration: false, // is default value after Electron v5
      contextIsolation: true, // protect against prototype pollution
      enableRemoteModule: false, // turn off remote
    },
  });

  mainWindow.on("page-title-updated", function (e) {
    e.preventDefault();
  });

  mainWindow.webContents.on("new-window", function (e, url) {
    e.preventDefault();
    require("electron").shell.openExternal(url);
  });

  if (process.platform === "darwin") {
    app.dock.setIcon(path.join(__dirname, filename));
  }

  mainWindow.loadFile(htmlfile);

  mainWindow.webContents.openDevTools();

  mainWindow.on("closed", function () {
    mainWindow = null;
  });

  // checkUtils.checkPort(() => {
  // });
}

app.on("ready", createWindow);

app.on("window-all-closed", function () {
  if (!isDev) {
    fs.writeFileSync(path.join(apphome, "boot.pid"), "stop_the_server");
  }
  app.quit();
  // if (process.platform !== "darwin") {
  // }
});

app.on("activate", function () {
  if (mainWindow === null) createWindow();
});

// avoid opening multiple instances
// const { app } = require('electron')
// let myWindow = null

// const gotTheLock = app.requestSingleInstanceLock()

// if (!gotTheLock) {
//   app.quit()
// } else {
//   app.on('second-instance', (event, commandLine, workingDirectory) => {
//     // Someone tried to run a second instance, we should focus our window.
//     if (myWindow) {
//       if (myWindow.isMinimized()) myWindow.restore()
//       myWindow.focus()
//     }
//   })

//   // Create myWindow, load the rest of the app, etc...
//   app.on('ready', () => {
//   })
// }
