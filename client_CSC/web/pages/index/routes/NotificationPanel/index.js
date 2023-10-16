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
  PopoverInteractionKind,
  NavbarDivider,
  NavbarGroup,
  Alignment,
  Classes,
  Icon,
  Popover,
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
import NoMessageForNotification from "../../components/NoMessageForNotification/index";
import LoadingShortPanel from "../../components/LoadingShortPanel/index";
import ControlBar from "../../components/control_bar/index.js";
import ControlTable from "../../components/control_table/index.js";
import Control_Pagination from "../../components/control_Pagination/index.js";

export default observer(() => {
  const msgPanelData = gstore.msgPanelData;
  let localStore = {
    ...gstore.msgPanelData,
    func: {
      search: gutils.api.msg.readCurrentNotifcation,
    },
  };
  return msgPanelData.pageCount == 0 ? (
    <NoMessageForNotification />
  ) : (
    <div className="control-table-notfiy-wrapper">
      <div class="simple-group-text">
        <div class="simple-left">Latest Messages</div>
        <div class="simple-right">
          <ButtonGroup>
            <Button
              icon="refresh"
              onClick={() => {
                localStore.func.search();
              }}
              intent="primary"
              loading={localStore.loading}
              outlined={true}
            >
              {"Refresh"}
            </Button>
            <Button
              icon="bookmark"
              onClick={() => {
                gutils.api.msg.markReadAllLatestMessages();
              }}
              intent="success"
              loading={localStore.loading}
              outlined={true}
            >
              {"Mark Read"}
            </Button>
            <Button
              icon="trash"
              onClick={() => {
                gutils.api.msg.cleanAllLatestMessages();
              }}
              intent="danger"
              loading={localStore.loading}
              outlined={true}
            >
              {"Clean All"}
            </Button>
          </ButtonGroup>
        </div>
      </div>
      <ControlTable
        leftfix={0}
        fixedColWidthArr={[50, 245, 157, 95, 151]}
        cols={gutils.genCol(
          [
            {
              label: "ID",
              value: "ID",
            },
            {
              label: "Title",
              value: "TITLE",
            },
            {
              label: "Content",
              value: (x) => {
                return (
                  <div>
                    <Popover
                      {...gutils.commonPopover()}
                      content={<div>{x["TEXT_CONTENT"]}</div>}
                      target={
                        <a href="javascript:void(0);">
                          {_.truncate(x["TEXT_CONTENT"], {
                            length: "The Message xxxxxxxxx".length,
                          })}
                        </a>
                      }
                    ></Popover>
                  </div>
                );
              },
            },
            {
              label: "Has Read?",
              value: (x) => (x.HAS_READ ? "Yes" : "No"),
            },
            {
              label: "Create Time",
              // value: (x) => gutils.formatDate(new Date(x["CREATE_TIME"])),
              value: "CREATE_TIME_DESC",
            },
          ],
          localStore.pageData
        )}
        tableInfo={localStore}
      ></ControlTable>
      <Control_Pagination tableInfo={localStore}></Control_Pagination>
    </div>
  );
});
