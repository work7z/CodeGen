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
import gutils from "../../utils";
import { useState } from "react";
import {
  useStores,
  useAsObservableSource,
  useLocalStore,
  useObserver,
} from "mobx-react-lite";
import { useEffect } from "react";
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
import gstore from "../../store.js";
import "./index.less";

export default () => {
  gutils.once("checking-network", () => {
    let doFunc = () => {
      gstore.preliAllData.configs.mirror = gutils.isChinaUser
        ? "china"
        : "global";
    };
    let isChinaUser = localStorage.getItem("isChinaUser");
    if (isChinaUser == null || window.api.dev) {
      // TODO: need cache this result to local
      axios({ url: "https://google.com", method: "HEAD", timeout: 3000 })
        .then((e) => {
          gutils.isChinaUser = false;
          localStorage.setItem("isChinaUser", "false");
          console.log("not china user");
          doFunc();
        })
        .catch((e) => {
          gutils.isChinaUser = true;
          localStorage.setItem("isChinaUser", "true");
          console.log("china user");
          doFunc();
        });
    } else {
      gutils.isChinaUser = isChinaUser == "yes";
      doFunc();
    }
  });
  var history = useHistory();
  gutils.defer(() => {
    function pushFunc() {
      const defaultHome = "/board/server/static/added";
      history.push(
        // window.ipc.dev ? "/board/server/proxy/added" : defaultHome
        // window.ipc.dev ? "/board/server/static/added" : defaultHome
        window.ipc.dev ? "/board/database/connections" : defaultHome
        // window.ipc.dev ? "/board/server/proxy/view?id=2" : defaultHome
        // window.ipc.dev
        //   ? "/board/server/static/view?id=5"
        //   : defaultHome
      );
    }
    const notStart = window.ipc.isLocalServerNotStartedUp();
    const isBrandNew = window.ipc.isBrandNewAndNeedDownloadInfra();
    if (isBrandNew || notStart) {
      gutils.waitInitializeRefFunc.push(async function () {
        pushFunc();
      });
      if (notStart && !isBrandNew) {
        history.push("/loadService");
        gutils.once("loadservice-callfunc", () => {
          gutils.api.system.bootService();
        });
      } else if (isBrandNew) {
        history.push("/prelimnary");
      }
    } else {
      pushFunc();
    }
  });
  return (
    <div className={"loading-box showloading"}>
      <div>Application is Loading...</div>
    </div>
  );
};
