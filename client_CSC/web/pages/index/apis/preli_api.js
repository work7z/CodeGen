import {
  Callout,
  PanelStack,
  ProgressBar,
  AnchorButton,
  Tooltip,
  Dialog,
  Drawer,
  Overlay,
  Alert,
  RadioGroup,
  Radio,
  ButtonGroup,
  TextArea,
  Intent,
  Position,
  Toaster,
  Checkbox,
  NumericInput,
  FormGroup,
  HTMLSelect,
  ControlGroup,
  InputGroup,
  Navbar,
  NavbarHeading,
  NonIdealState,
  NavbarDivider,
  NavbarGroup,
  Alignment,
  Classes,
  Icon,
  Card,
  Elevation,
  Button,
} from "@blueprintjs/core";
import { Example, IExampleProps } from "@blueprintjs/docs-theme";
import {
  ColumnHeaderCell,
  Cell,
  Column,
  Table,
  Regions,
} from "@blueprintjs/table";
import React from "react";
import ReactDOM from "react-dom";
import gutils from "../utils";
import { useState } from "react";
import {
  useStores,
  useAsObservableSource,
  useLocalStore,
  useObserver,
} from "mobx-react-lite";
import { Provider, observer, inject } from "mobx-react";
var createHistory = require("history").createBrowserHistory;
import {
  withRouter,
  HashRouter as Router,
  Switch,
  Route,
  Link,
  useHistory,
} from "react-router-dom";
var { autorun, observable } = require("mobx");
import gstore from "../store.js";
import LoadingPage from "../routes/loading/index";
import MainPage from "../routes/main/index";
import "../index.less";
import _, { reject } from "lodash";

const myapi = {
  getFileName(str) {
    return str.substring(str.lastIndexOf("/") + 1);
  },
  downloadingSDKRuntime: async function (repoJSON, { isChinaUser, mirror }) {
    let checkingStatus = gstore.preliAllData.checkingStatus;
    checkingStatus.logs.runtime.work = true;
    checkingStatus.logs.runtime.msg = [
      {
        ok: false,
        fullText: "Start downloading the runtime for local service...",
      },
    ];
    const { ipc } = window;
    const downloadDir = ipc.getAppDownloadPath();
    let targetSDKFile = repoJSON.runtime[mirror][ipc.platform];
    const downloadPath =
      downloadDir +
      ipc.sep +
      "runtime" +
      ipc.sep +
      myapi.getFileName(targetSDKFile);

    console.log("xx downloading SDK file", { targetSDKFile, downloadPath });

    await new Promise((resolve, reject) => {
      ipc.downloadFile(targetSDKFile, downloadPath, {
        onProgress: (e) => {
          if (checkingStatus.interrupt) {
            e.cancel();
            checkingStatus.tryStopLoading = false;
            reject("interrupted by user");
            return;
          }
          if (
            checkingStatus.logs.core.error ||
            checkingStatus.logs.runtime.error
          ) {
            e.cancel();
            return;
          }
          checkingStatus.logs.runtime.msg = [
            {
              ok: false,
              label: "runtime file",
              e: e,
            },
          ];
        },
        onSuccess(e) {
          checkingStatus.logs.runtime.msg = [
            {
              ok: true,
              label: "runtime file",
              e: e,
            },
          ];
          resolve();
        },
        onFail(e) {
          console.log("got error", e);
          checkingStatus.logs.runtime.error = {
            label: "Cannot download the runtime file",
            cause: e.message || e,
          };
          checkingStatus.logs.hasError = true;
          reject(e);
        },
      });
    });

    // start decompressing the JRE
    try {
      let newMsgItem = {
        ok: false,
        fullText: "Decompressing the runtime archive file...",
      };
      checkingStatus.logs.runtime.msg.push(newMsgItem);
      await ipc.decompress(downloadPath);
      checkingStatus.logs.runtime.msg[1].ok = true;
      checkingStatus.logs.runtime.msg[1].fullText =
        "Decompressed the runtime archive file";
      ipc.rm(downloadPath);
    } catch (e) {
      checkingStatus.logs.runtime.error = {
        label: "Cannot decompress the runtime file",
        cause: e.message || e,
      };
    }
  },
  downloadingMavenCore: async function (repoJSON, { isChinaUser, mirror }) {
    let checkingStatus = gstore.preliAllData.checkingStatus;
    checkingStatus.logs.core.work = true;
    checkingStatus.logs.core.msg = [
      {
        fullText:
          "Start downloading the core dependencies for local service...",
        ok: false,
      },
    ];
    const { ipc } = window;
    const downloadDir = ipc.getAppDownloadPath();
    const usingMavenMirror = _.shuffle(repoJSON.core[mirror])[0];
    const jars = repoJSON.jars;
    console.log("xx using maven mirror ", usingMavenMirror, jars);

    let idx = 0;
    for (let jarShortURL of jars) {
      idx++;
      const completeURL = usingMavenMirror + jarShortURL;
      const downloadPath =
        downloadDir +
        ipc.sep +
        "core" +
        ipc.sep +
        myapi.getFileName(completeURL);
      await new Promise((resolve, reject) => {
        ipc.downloadFile(completeURL, downloadPath, {
          onProgress: (e) => {
            if (checkingStatus.interrupt) {
              e.cancel();
              checkingStatus.tryStopLoading = false;
              reject("interrupted by user");
              return;
            }
            if (
              checkingStatus.logs.core.error ||
              checkingStatus.logs.runtime.error
            ) {
              e.cancel();
              reject("interrupted by user");
              return;
            }
            checkingStatus.logs.core.msg = [
              {
                ok: false,
                label: "core file" + `(${idx}/${_.size(jars)})`,
                e: e,
              },
            ];
            // checkingStatus.logs.core.msg = `Downloading the core file (${idx}/${_.size(
            //   jars
            // )}), status: ${e.rate}(${e.loaded}/${e.total})`;
            // if (e.loaded == e.total) {
            //   resolve();
            // }
          },
          onSuccess(e) {
            checkingStatus.logs.core.msg = [
              {
                ok: true,
                label: "core file" + `(${idx}/${_.size(jars)})`,
                e: e,
              },
            ];
            resolve();
          },
          onFail(e) {
            checkingStatus.logs.core.error = {
              label: "Cannot download the core file",
              cause: e.message || e,
            };
            checkingStatus.logs.hasError = true;
            reject(e);
          },
        });
      });
    }
  },
  startInit: async function () {
    let checkingStatus = gstore.preliAllData.checkingStatus;
    _.merge(checkingStatus, gstore.preliAllData.getCheckingStatus());
    _.merge(checkingStatus, {
      ...gstore.preliAllData.getCheckingStatus(),
    });
    checkingStatus.start = true;
    const { ipc } = window;
    const downloadDir = ipc.getAppDownloadPath();
    // init, deleting all of these codegen_repository files
    checkingStatus.logs.init.work = true;
    checkingStatus.logs.init.msg = [
      {
        ok: false,
        fullText: "Checking the directory for downloading files",
      },
    ];
    ipc.deleteDir(downloadDir);
    ipc.mkdirDir(downloadDir);
    checkingStatus.logs.init.msg = [
      {
        ok: true,
        fullText: "Checked the directory for downloading files",
      },
    ];
    const isChinaUser = gstore.preliAllData.configs.mirror == "china";
    // download sdk and maven
    let repoJSON = ipc.getRepoJSON();
    let arr = [
      myapi.downloadingMavenCore(repoJSON, {
        isChinaUser,
        mirror: gstore.preliAllData.configs.mirror,
      }),
      myapi.downloadingSDKRuntime(repoJSON, {
        isChinaUser,
        mirror: gstore.preliAllData.configs.mirror,
      }),
      (async function () {
        // handling unzipping local client jar file
        checkingStatus.logs.local.work = true;
        const clientJarFilePath = ipc.getClientJarFilePath();
        const finalJarDecompressDir = ipc.getAppDownloadPathByJoin("client");
        checkingStatus.logs.local.msg = [
          {
            ok: false,
            fullText: "Decompressing the local service files...",
          },
        ];
        // await ipc.decompress(clientJarFilePath, finalJarDecompressDir);
        checkingStatus.logs.local.msg = [
          {
            ok: true,
            fullText: "Decompressed the local service files",
          },
        ];
      })(),
    ];
    for (let item of arr) {
      await item;
    }

    gstore.preliAllData.checkingStatus.done = true;
    let configJSONStr = JSON.stringify(gstore.preliAllData.configs);
    ipc.saveInitJson(configJSONStr);
  },
  stopInit: async function () {
    let checkingStatus = gstore.preliAllData.checkingStatus;
    _.merge(checkingStatus, {
      // ...gstore.preliAllData.getCheckingStatus(),
      tryStopLoading: true,
      interrupt: true,
    });
  },
};

export default myapi;
