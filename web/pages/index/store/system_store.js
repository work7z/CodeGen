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
import gutils from "../utils";

function iterateTreeForRoadMap(arr, loopFunc, nextKey = "children") {
  return _.map(arr, (x, d, n) => {
    loopFunc(x, d, n);
    if (x[nextKey]) {
      x[nextKey] = iterateTreeForRoadMap(x[nextKey], loopFunc, nextKey);
    }
    return x;
  });
}

let previousLocalSettings = localStorage.getItem("LOCAL_SETTINGS");
let localSettings = {
  isLeftMenuOpen: true,
  leftMenuWidth: "250px",
  database_topview_left_project_width: "250px",
  database_top_bottom_view_project_width: "50%",
};
// _.forEach(localSettings, (x, d, n) => {
//   localSettings[d + "_bck"] = x;
// });
window.RAW_LOCALSETTING = _.cloneDeep(localSettings);
function safeparse(str) {
  try {
    return JSON.parse(str);
  } catch (err) {
    return null;
  }
}
if (previousLocalSettings != null && !window.ipc.dev) {
  let ok = safeparse(previousLocalSettings);
  _.merge(localSettings, ok);
}

export default {
  bootServer: {
    text: "",
    err: null,
  },
  localSettings: localSettings,
  settings: {
    drawerConfig: {
      open: false,
      view_key: "general",
    },
    model: {},
    loading: false,
    dev_mode: false,
    other: {
      temp_model: {},
      ...constants.extraModelProps(),
      addModelFailures: {},
      isAddModelPass: true,
    },
    alerts: {},
  },
  sysinfo: {
    latestRoutePath: null,
    breadmenu: null,
    roadmapHighlightPath: null,
    latestNewMsgCount: 0,
    // isOpenNotification: false,
    isOpenNotification: false,
  },
  msgPanelData: {
    loading: false,
    formModel: {},
    alertType: "create",
    addModelFailures: {},
    isAddModelPass: false,
    initModel: constants.initModel(),
    addModel: constants.initModel(),
    formNeeds: {
      groups: [],
    },
    pageData: [],
    pageCount: 0,
    pageInfo: {
      pageIndex: 1,
      pageSize: 25,
    },
  },
  roadmap: iterateTreeForRoadMap(
    [
      // {
      //   label: "Dashboard",
      //   icon: "application",
      //   children: [
      //     {
      //       label: "Home",
      //       icon: "send-to-map",
      //       pathname: "/dashboard/home",
      //     },
      //     // {
      //     //   label: "Settings",
      //     //   icon: "settings",
      //     //   pathname: "/dashboard/settings",
      //     // },
      //   ],
      // },
      {
        label: "Utility Server",
        icon: "globe",
        children: [
          {
            label: "Static Server",
            icon: "mountain",
            expand: false,
            children: [
              {
                icon: "panel-table",
                label: "Added",
                pathname: "/server/static/added",
              },
              {
                icon: "console",
                label: "View Panel",
                pathname: "/server/static/view",
              },
            ],
          },
          {
            label: "Proxy Server",
            icon: "pulse",
            expand: false,
            children: [
              {
                label: "Added",
                icon: "panel-table",
                pathname: "/server/proxy/added",
              },
              {
                icon: "console",
                label: "View Panel",
                pathname: "/server/proxy/view",
              },
            ],
          },
        ],
      },
      {
        label: "Code Generator",
        icon: "heat-grid",
        children: [
          // {
          //   label: "Spring",
          //   pathname: "/gen/spring",
          //   icon: "array-numeric",
          // },
          {
            label: "MyBatis",
            pathname: "/gen/mybatis",
            icon: "array",
          },
          // {
          //   label: "React",
          //   pathname: "/gen/react",
          //   icon: "array-numeric",
          // },
          // {
          //   label: "Vue",
          //   pathname: "/gen/vue",
          //   icon: "array-boolean",
          // },
        ],
      },
      {
        label: "Database Tools",
        icon: "database",
        children: [
          {
            label: "Connections",
            pathname: "/database/connections",
            icon: "swap-horizontal",
          },
          // {
          //   label: "Data Console",
          //   pathname: "/db/console",
          //   icon: "console",
          // },
          {
            label: "Data Import",
            pathname: "/db/import",
            icon: "import",
          },
          {
            label: "Data Export",
            pathname: "/db/export",
            icon: "export",
          },
        ],
      },
    ],
    (x) => {
      if (!_.isEmpty(x.children) && x.expand !== false) {
        x.expand = true;
      }
      if (x.pathname) {
        x.pathname = "/board" + x.pathname;
      }
      x.id = parseInt(_.uniqueId());
    }
  ),
};
