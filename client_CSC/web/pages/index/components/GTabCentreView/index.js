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
  ContextMenu,
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
  Tree,
  Icon,
  Card,
  Elevation,
  Button,
  PanelStack2,
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
import {
  Classes as Popover2Classes,
  ContextMenu2,
  Tooltip2,
} from "@blueprintjs/popover2";
import _ from "lodash";

export default observer((props) => {
  const { viewObj, viewKey } = props;
  const activeKey = viewObj[viewKey];
  return (
    <Card className={"g-tab-card-view"}>
      <div className="g-tab-centre-view">
        <div
          className={`g-tab-centre-header g-tab-card-header-${_.size(
            props.tab
          )}-len`}
        >
          {_.map(props.tab, (x, d, n) => {
            return (
              <div
                className={
                  "g-tab-centre-item" +
                  " " +
                  (x.id == activeKey ? "active-tab-centre" : "")
                }
                key={d}
                onClick={() => {
                  viewObj[viewKey] = x.id;
                }}
              >
                {x.label}
              </div>
            );
          })}
        </div>
        <div className="g-tab-centre-body">
          {_.map(props.tab, (x, d, n) => {
            if (x.id == activeKey) {
              return x.jsx();
            } else {
              return "";
            }
          })}
        </div>
      </div>
    </Card>
  );
});
