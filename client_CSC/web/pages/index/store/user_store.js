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

const initModelForPageData = () => {
  return {
    username: null,
    password: null,
    verificationCode: null,
  };
};

export default {
  overlayForLogin: {
    ...constants.commonPropsForDialog(),
    title: "Sign In",
    name: "N/A",
    open: false,
    confirmIntent: "primary",
    confirmText: "Confirm",
  },
  overlayForUserInfo: {
    ...constants.commonPropsForDialog(),
    title: "Welcome, " + localStorage.getItem("SYS_USER_NAME"),
    name: "N/A",
    open: false,
    confirmIntent: "primary",
    confirmText: "Confirm",
  },
  loginPageData: {
    loading: false,
    toggle_status_loading: false,
    alertType: "create",
    addModelFailures: {},
    isAddModelPass: false,
    initModel: initModelForPageData(),
    addModel: initModelForPageData(),
    formNeeds: {},
    pageData: [],
    pageCount: 0,
    pageInfo: {
      pageIndex: 1,
      pageSize: constants.COMMON_SIZE,
    },
  },
  loginInfo: {
    token: null,
  },
  usermodel: {},
};
