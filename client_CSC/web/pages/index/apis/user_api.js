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

const myapi = {
  login: async function () {
    try {
      gstore.user.overlayForLogin.loading = true;
      let param = gstore.user.loginPageData.addModel;
      let signInRes = await gutils.optCentre("/user/post/sign-in", param);
      let ftlErrInfoStr = _.chain(signInRes).get("ftlMap.errInfo").value();
      gstore.user.overlayForLogin.loading = false;
      if (ftlErrInfoStr) {
        _.forEach(ftlErrInfoStr, (x, d, n) => {
          gutils.alert({
            message: x || "Cannot executed your request",
            intent: "danger",
            icon: "error",
          });
        });
        gstore.user.loginPageData.addModel.EXTRA_TMP_LOGIC_vcode =
          Math.random();
        return;
      } else {
        gutils.alertOk("Sign In Successfully");
        localStorage.setItem("USER_TOKEN", signInRes.TOKEN);
        let cloneModel = _.cloneDeep(param);
        cloneModel.password = "";
        localStorage.setItem("USER_INFO", JSON.stringify(cloneModel));
        location.reload();
      }
    } catch (e) {
      console.log("got error when login", e);
      gstore.user.overlayForLogin.loading = false;
    }
  },
};

export default myapi;
