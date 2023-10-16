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
import { useState, useEffect } from "react";
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
import GEditor from "../../components/GEditor";
import ControlTable from "../../components/control_table";
import { Resizable } from "re-resizable";
import GTabs from "../../components/GTabs/index";
import DbConnProject from "../../routes/db_conn_project";
import Dblink_tab_overview from "../dblink_tab_overview";

export default observer(() => {
  const ID_DB_VIEW = "ID_DB_VIEW";
  let crtColumns = [];
  let localStore = {
    ...gstore.databaseAllData.pageData,
    func: {
      search: () => {},
    },
  };

  {
    /* 
    /> */
  }
  useEffect(() => {
    gutils.api.dblink.loadEditor();
    gutils.api.dblink.loadConnTree();
    return () => {};
  }, []);
  return (
    <div className="dblink-page">
      <Card
        className="dbAllContent"
        ref={(e) => {
          console.log("llcard", e);
        }}
      >
        <Resizable
          enable={_.merge(gutils.enableResize(), {
            bottom: true,
          })}
          style={{}}
          className="dbEditorView"
          defaultSize={{
            width: "100%",
            height: gstore.localSettings.database_top_bottom_view_project_width,
          }}
          onResizeStop={(event, direct, refToEle, delta) => {
            gutils.defer(() => {
              gstore.localSettings.database_top_bottom_view_project_width =
                refToEle.style.height;
            });
          }}
        >
          <div className="editor-conbine-box h100p just-flex">
            <Resizable
              enable={_.merge(gutils.enableResize(), {
                right: true,
              })}
              className="editor-left-project"
              {...gutils.resizeEvent({
                size: {
                  height: "100%",
                },
                obj: gstore.localSettings,
                key: "database_topview_left_project_width",
              })}
            >
              <DbConnProject />
            </Resizable>
            <div
              className="editor-right-content"
              style={{
                width: `calc(100% - ${gstore.localSettings.database_topview_left_project_width})`,
              }}
            >
              <GTabs
                obj={gstore.databaseAllData.data.editorTab}
                renderTabPane={(x, d, n) => {
                  switch (x.type) {
                    case "overview":
                      return <Dblink_tab_overview />;
                      break;
                  }
                  return (
                    <div>
                      <GEditor
                        style={{
                          width: "100%",
                          height: "100%",
                          border: "1px solid #ccc",
                        }}
                        id={ID_DB_VIEW + d}
                      />
                    </div>
                  );
                }}
              />
            </div>
          </div>
        </Resizable>

        <div
          className="dbDataView"
          style={{
            height: `calc(100% - ${gstore.localSettings.database_top_bottom_view_project_width})`,
            maxHeight: `calc(100% - ${gstore.localSettings.database_top_bottom_view_project_width})`,
          }}
        >
          <GTabs
            obj={gstore.databaseAllData.data.dataViewTab}
            renderTabPane={(x, d, n) => {
              return (
                <div className="control-tb-view">
                  <ControlTable
                    type="epoch"
                    cols={gutils.genCol(crtColumns, localStore.pageData)}
                    tableInfo={localStore}
                    leftfix={0}
                  ></ControlTable>
                </div>
              );
            }}
          />
        </div>
      </Card>
    </div>
  );
});
