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
import gutils from "./utils";
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
import system_store from "./store/system_store";
import static_server_store from "./store/static_server_store";
import proxy_server_store from "./store/proxy_server_store";
import database_store from "./store/database_store";
import preliminary_store from "./store/preliminary_store";
import User_store from "./store/user_store";

class SysStore {
  // user
  @observable user = User_store;

  // static
  @observable staticOverlay = {
    deleteItem: static_server_store.overlay.deleteServer,
    addItem: static_server_store.overlay.addItem,
  };
  @observable static_view_detail_console = static_server_store.consoleData;
  @observable staticServerPageData = static_server_store.pageData;

  // proxy
  @observable proxyOverlay = {
    deleteItem: proxy_server_store.overlay.deleteServer,
    addItem: proxy_server_store.overlay.addItem,
    addRule: proxy_server_store.overlay.addRule,
    addRulePathRewrite: proxy_server_store.overlay.addRulePathRewrite,
  };
  @observable proxy_view_detail_console = proxy_server_store.consoleData;
  @observable proxyServerPageData = proxy_server_store.pageData;
  @observable proxyServerPageDataForRule = proxy_server_store.pageDataForRule;
  @observable proxyServerPageDataForPathRewrite =
    proxy_server_store.pageDataForPathRewrite;

  // system
  @observable roadmap = system_store.roadmap;
  @observable sysinfo = system_store.sysinfo;
  @observable sysBootServer = system_store.bootServer;
  @observable settings = system_store.settings;
  @observable localSettings = system_store.localSettings;
  @observable msgPanelData = system_store.msgPanelData;
  @observable databaseAllData = database_store;
  @observable preliAllData = preliminary_store;
}
let gstore = new SysStore();
if (window.api.dev) {
  window.gstore = gstore;
}
export default gstore;
