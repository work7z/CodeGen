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
import _ from "lodash";
var { autorun, observable } = require("mobx");
import constants from "../constants";

function getCheckingStatus() {
  return {
    start: false,
    done: false,
    doneText: "Finish",
    hasError: false,
    tryStopLoading: false,
    interrupt: false,
    logs: {
      init: {
        work: false,
        msg: null,
        error: null,
      },
      runtime: {
        work: false,
        msg: null,
        error: null,
      },
      core: {
        work: false,
        msg: null,
        error: null,
      },
      local: {
        work: false,
        msg: null,
        error: null,
      },
    },
  };
}

export default {
  configs: {
    isUserInChina: false,
    lang: "en_US",
    mirror: "global",
  },
  getCheckingStatus,
  checkingStatus: getCheckingStatus(),
  formList: {
    mirrors: [
      {
        label: "Global Network Mirror",
        value: "global",
      },
      {
        label: "China Network Mirror(国内加速源)",
        value: "china",
      },
    ],
    lang: [
      {
        label: "English",
        value: "en_US",
      },
      {
        label: "简体中文(待完成)",
        value: "zh_CN",
        disabled: true,
      },
      {
        label: "繁體中文(待完成)",
        value: "zh_HK",
        disabled: true,
      },
    ],
  },
};
