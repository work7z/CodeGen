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
import _ from "lodash";

const reqInfo = {
  lastCount: -1,
};

const myapi = {
  bootService: async () => {
    console.log("start booting the server");
    const t1 = "The Local Service is Loading...";
    gstore.sysBootServer.text = t1;
    gstore.sysBootServer.err = null;
    // finding the available ports
    let finalPortValue = -1;
    for (let i = 35000; i < 50000; i++) {
      let checkArr = [i, i + 1, i + 2, i + 3, i + 4, i + 5];
      let isAnyErrorHas = false;
      for (let checkPort of checkArr) {
        try {
          const iscrtuse = await ipc.getAvailablePort(checkPort);
          if (iscrtuse) {
            isAnyErrorHas = true;
          }
        } catch (err) {
          console.log("got failed");
          isAnyErrorHas = true;
        }
      }
      if (!isAnyErrorHas) {
        finalPortValue = i;
        window.ipc.setPort(i);
        break;
      }
    }
    if (finalPortValue == -1) {
      gstore.sysBootServer.text = `Sorry, cannot the available service ports on this PC. Please check if the network interface is working normally.`;
      gstore.sysBootServer.err = "No available listen ports to be used";
      return;
    }

    // run the process
    let cmd = ipc.getRunMainBackendTotalCmd(finalPortValue);
    await new Promise((resolve, reject) => {
      let gotLogs = {
        ctn_data: 0,
        err: "",
        close: "",
      };
      ipc.execCommand(cmd, {
        data(str) {
          gotLogs.ctn_data += _.size(str);
          console.log("data", str);
          gstore.sysBootServer.text = `[${gotLogs.ctn_data}] ` + t1;
          if (str.indexOf("APP_BOOT_DONE") != -1) {
            resolve();
          }
        },
        err(error) {
          console.log("error", error);
          gotLogs.err += error;
        },
        close(str) {
          console.log("close", str);
          gotLogs.close += str;
          if (str == false && !_.isEmpty(gotLogs.err)) {
            gstore.sysBootServer.text = `Sorry, cannot boot the local service. If this issue still exists after having re-tried, please contact us work7z@outlook.com`;
            gstore.sysBootServer.err = gotLogs.err;
          }
        },
      });
      (async function () {
        while (true) {
          try {
            let res = await gutils.opt(
              "/system/health-check",
              {},
              {
                port: finalPortValue,
              }
            );
            console.log("finish the boot", res);
            resolve();
          } catch (err) {
            console.log("not inited", err);
          }
          await gutils.sleep(1000);
        }
      })();
    });

    window.ipc.writeBootLog();

    gstore.sysBootServer.text = `The Local Service has been Booted.`;
    gstore.sysBootServer.err = null;
    console.log("finished this boot process");

    location.reload();
  },
  restoreAllSettings: async () => {
    if (
      !window.confirm(
        "Are you sure that you want to restore all of these system settings? It means that the previous customer settings will be reinstated to the factory settings"
      )
    ) {
      return;
    }
    await gutils.opt("/system/setting-restore");
    gutils.alertOk("Restored system settings successfully");
    setTimeout(() => {
      location.reload();
    }, 1000);
  },
  openSettingAPI: async (view_key) => {
    gstore.settings.drawerConfig.open = true;
    gstore.settings.drawerConfig.view_key = view_key;
    gstore.settings.other.temp_model = gutils.clone(gstore.settings.model);
  },
  confirmAndUpdateSetting: async () => {
    gstore.settings.loading = true;
    gstore.settings.model = {
      ...gutils.clone(gstore.settings.other.temp_model),
    };
    await gutils.opt("/system/setting-save-all", gstore.settings.model);
    gutils.alertOk(
      "Updating the settings successfully, some of these config will be effected after the reboot of the application."
    );
    await gutils.api.system.preInit.loadingAllSettings();
    gstore.settings.loading = false;
  },
  preInit: {
    loadingAllSettings: async () => {
      await gutils.opt("/infra/close-download", {
        type: ["system", "db-driver"],
      });
      let res = await gutils.opt("/system/setting-query-all");
      let finModel = {};
      _.forEach(res.content, (x, d, n) => {
        finModel[x.MYKEY] = x.MYVALUE;
      });
      gstore.settings.model = finModel;
      let devModeRes = await gutils.opt("/system/dev-mode");
      gstore.settings.dev_mode = devModeRes.content;
    },
  },
  // if want to auto run or init, add methods below
  init: {
    keepUpdatingLatestMsgCount: async () => {
      while (true) {
        try {
          await gutils.api.msg.runSingleUpdatingMsgCount();
          await gutils.sleep(500);
          if (gstore.sysinfo.isOpenNotification) {
            await gutils.api.msg.readCurrentNotifcation();
          }
        } catch (e) {
          console.log("got error", e);
          gutils.defer(() => {
            if (gutils.hist.location.pathname != "/handling") {
              gutils.opt("/system/health-check").catch((e) => {
                let localCheckFunc = async () => {
                  while (true) {
                    try {
                      let res = await gutils.opt("/system/health-check");
                      console.log("check res", res);
                      if (res && res.status) {
                        break;
                      }
                    } catch (e) {
                      console.log("still failed", e);
                    }
                    await gutils.sleep(1000);
                  }
                  console.log("system resumed");
                  window.location.reload();
                };
                localCheckFunc();
                gutils.hist.push("/handling");
              });
            }
          });
          await gutils.sleep(1000);
        }
      }
    },
  },
};
export default myapi;
