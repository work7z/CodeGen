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
import { INTENT_PRIMARY } from "@blueprintjs/core/lib/esm/common/classes";
import "./index.less";
import ControlBar from "../../components/control_bar/index.js";
import ControlTable from "../../components/control_table/index.js";
import Control_Pagination from "../../components/control_Pagination/index.js";
import { intentClass } from "@blueprintjs/core/lib/esm/common/classes";

export default observer(() => {
  let localStore = {
    ...gstore.staticServerPageData,
    func: {
      search: gutils.api.static.loadAddedList,
      create: () => {
        gutils.api.static.openAddingModal("create");
      },
    },
  };
  gutils.once("static-added", () => {
    gutils.api.static.loadAddedList();
  });
  const labelCols = [
    { label: "Server Name", value: "NAME" },
    {
      label: "Status",
      value: (x) => {
        const status = "" + x.RUN_STATUS;
        const map = gutils.renderingStaticRunStatus();
        let finval = map[status];
        // if (status == 0) {
        //   return finval.label;
        // }
        return (
          <Link
            to={"/board/server/static/view?id=" + x["ID"]}
            style={{ color: finval.color }}
          >
            {finval.label}
          </Link>
        );
      },
    },
    { label: "Group Name", value: "FOLDER_NAME" },
    {
      label: "Bind Address",
      value: "LOCAL_LISTEN_IPADDR",
    },
    {
      label: "Listen Port",
      value: (x) => {
        return x["LOCAL_LISTEN_PORT"];
        // let listenPort = x["LOCAL_LISTEN_PORT"];
        // let listenSSLPort = x["LOCAL_LISTEN_SSL_PORT"];
        // let totalReturn = [];
        // if (listenPort) {
        //   totalReturn.push("HTTP:" + listenPort);
        // }
        // if (listenSSLPort) {
        //   totalReturn.push("HTTPS:" + listenSSLPort);
        // }
        // return totalReturn.join(",");
      },
    },
    { label: "Description", value: "BRIEF" },
    { label: "Context Path", value: "CONTEXT_PATH" },
    { label: "Directory", value: "FILE_PATH" },
    {
      label: "List Directory?",
      value: (x) => (x.LIST_DIRECTORY ? "Yes" : "No"),
    },
    {
      label: "Using SSL?",
      value: (x) => (x.IS_LOCAL_SSL == 1 ? "Yes" : "No"),
    },
    {
      label: "AutoRun?",
      value: (x) => (x.BOOT_FLAG ? "Yes" : "No"),
    },
    {
      label: "Create Time",
      // value: (x) => gutils.formatDate(new Date(x["CREATE_TIME"])),
      value: "CREATE_TIME_DESC",
    },
    {
      label: "Operation",
      value: (x) => {
        const runStatusDefineObj = gutils.runStatusDefineObj();
        const runStatusViewColor = gutils.runStatusViewColor();
        return (
          <div className="between-anchor">
            <a
              href={gutils.void_ref}
              style={{
                color: runStatusViewColor[x.RUN_STATUS],
              }}
              onClick={() => {
                gutils.api.static.optMachine(x);
              }}
            >
              {runStatusDefineObj[x.RUN_STATUS]}
            </a>
            {x.RUN_STATUS == 2 ? (
              <a
                href={gutils.void_ref}
                style={{
                  color: runStatusViewColor[1],
                }}
                onClick={() => {
                  gutils.api.static.optMachine({
                    ID: x.ID,
                    RUN_STATUS: 1,
                    forceStop: true,
                  });
                }}
              >
                {runStatusDefineObj[1]}
              </a>
            ) : null}
            <Link
              onClick={() => {
                gutils.api.static.openAddingModal("update", x);
              }}
            >
              Edit
            </Link>
            <Link to={"/board/server/static/view?id=" + x["ID"]}>Console</Link>
          </div>
        );
      },
    },
  ];

  return (
    <Card>
      <div>
        <ControlBar
          folderkey="staticServerPageData"
          tableInfo={localStore}
          text="Static Added List"
        />
        <ControlTable
          cols={gutils.genCol(labelCols, localStore.pageData)}
          tableInfo={localStore}
        ></ControlTable>
        <Control_Pagination tableInfo={localStore}></Control_Pagination>
      </div>
    </Card>
  );
});
