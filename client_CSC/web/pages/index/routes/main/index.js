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
import MainNavBar from "../main_navbar/index";
import MainMenuFrame from "../main_menu_frame";
import Overlay_playground from "../overlay_playground";
import gapi from "../../gapi.js";

export default () => {
  gutils.defer(() => {
    let doneFunc = () => {
      gutils.once("once_api_run", () => {
        _.forEach(gapi.system.init, (x, d, n) => {
          x();
        });
      });
    };
    const isBrandNewAndNeedDownloadInfra =
      window.ipc.isBrandNewAndNeedDownloadInfra();
    if (
      isBrandNewAndNeedDownloadInfra ||
      window.ipc.isLocalServerNotStartedUp()
    ) {
      gutils.waitInitializeRefFunc.push(async function () {
        doneFunc();
      });
    } else {
      doneFunc();
    }
  });
  return (
    <div>
      <Overlay_playground />
      <MainNavBar />
      <MainMenuFrame />
    </div>
  );
};
