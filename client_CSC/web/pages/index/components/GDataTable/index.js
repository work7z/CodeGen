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
import ControlTable from "../control_table/index";
import { intentClass } from "@blueprintjs/core/lib/esm/common/classes";

export default observer((props) => {
  const ctlDataObj = props.ctlDataObj;
  const tableData = props.value;
  return (
    <div className="g-proxy-rules-wrapper">
      <Card>
        <ControlTable
          fixedColWidthArr={ctlDataObj.fixedColWidthArr}
          leftfix={ctlDataObj.leftfix || 0}
          cols={gutils.genCol(ctlDataObj.col, tableData)}
          size={_.max([_.size(tableData) || 0, 5])}
        ></ControlTable>
        {props.noAddBtn ? null : (
          <div style={{ overflow: "hidden" }}>
            <div style={{ float: "right" }}>
              <ButtonGroup>
                <Button
                  text={ctlDataObj.addText}
                  intent={Intent.NONE}
                  outlined={true}
                  onClick={() => {
                    ctlDataObj.addFunc();
                  }}
                />
              </ButtonGroup>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
});
