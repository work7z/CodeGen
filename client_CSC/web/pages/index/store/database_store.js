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
import _ from "lodash";
var { autorun, observable } = require("mobx");
import constants from "../constants";

const editorTabsArr = [];

for (let i = 1; i < 20; i++) {
  // editorTabsArr.push({
  //   label: "Untitled-" + i,
  //   id: "ut-" + i,
  // });
}

let fn_addConn = () => {
  return {
    ID: null,
    EXTRA_DATA_IS_SYS_IPT_NAME: null,
    IS_CONNECTION: null,
    CONNECTION_NAME: null,
    CONNECTION_BRIEF: null,
    FOLDER_ID: null,
    DBTYPE_ID: null,
    DRIVER_ID: null,
    JDBC_URL: null,
    USERNAME: null,
    PASSWORD: null,
    HOST: null,
    DEFAULT_DATABASE: null,
    PORT: null,
    SAVE_PASSWORD_LOCALLY: 1,
  };
};
let fn_addFolder = () => {
  return {
    PARENT_FOLDER_ID: null,
    ID: null,
    FOLDER_NAME: null,
    FOLDER_BRIEF: null,
  };
};

export default {
  addNewConnPageData: {
    viewkey: "main",
    viewkeyFolder: "main",
    isLoadingTestConn: false,
    loading: false,
    formModel: {},
    toggle_status_loading: false,
    alertType: "create",
    addModelFailures: {},
    isAddModelPass: false,
    initModel: fn_addConn(),
    addModel: fn_addConn(),

    initModelForFolder: fn_addFolder(),
    addModelForFolder: fn_addFolder(),

    formNeeds: {
      driver_downloadStatus: {
        desc: "",
        status: "prepare",
        currentSize: 0,
        totalSize: 0,
        errMsg: null,
      },
      dbTypes_loading: false,
      relatedDrivers_loading: false,
      dbTypes: [],
      relatedDrivers: [],
      driver_download_uid: null,
    },
    pageData: [],
    pageCount: 0,
    pageInfo: {
      pageIndex: 1,
      pageSize: constants.COMMON_SIZE,
    },
  },
  overlay_addNewConn: {
    ...constants.commonPropsForDialog(),
    title: "Add New Connection",
    open: false,
    confirmIntent: "primary",
    icon: "add-to-artifact",
    confirmText: "Confirm",
  },
  overlay_addNewFolder: {
    ...constants.commonPropsForDialog(),
    title: "Add New Folder",
    // open: true,
    confirmIntent: "primary",
    icon: "folder-new",
    confirmText: "Confirm",
  },
  data: {
    loadingTree: false,
    formModel: {
      loading: false,
    },
    editorTab: {
      value: "wlc",
      list: editorTabsArr,
    },
    dataViewTab: {
      value: "overview",
      list: [
        {
          id: "overview",
          label: "Overview",
        },
        {
          id: "ResultSet-1",
          label: "ResultSet-1",
        },
      ],
    },
    connectionList: {
      tree: [
        // {
        //   title: "Test Projects",
        //   key: "testprojects",
        //   isLeaf: false,
        //   children: [
        //     {
        //       title: "MySQL Local Server",
        //       key: "k1",
        //     },
        //     {
        //       title: "MySQL Remote Server",
        //       key: "k2",
        //     },
        //   ],
        // },
        // {
        //   title: "Oracle Projects",
        //   key: "op",
        //   isLeaf: false,
        //   children: [
        //     {
        //       title: "Oracle 测试数据库",
        //       key: "k3",
        //     },
        //   ],
        // },
        // {
        //   title: "Oracle Wrap 测试数据库",
        //   key: "k31",
        // },
      ],
    },
  },
  pageData: {
    loading: false,
    formModel: {},
    toggle_status_loading: false,
    alertType: "create",
    addModelFailures: {},
    isAddModelPass: false,
    initModel: constants.initModel(),
    addModel: constants.initModel(),
    formNeeds: {
      groups: [],
      netcards: [],
    },
    pageData: [],
    pageCount: 0,
    pageInfo: {
      pageIndex: 1,
      pageSize: constants.COMMON_SIZE,
    },
  },
};
