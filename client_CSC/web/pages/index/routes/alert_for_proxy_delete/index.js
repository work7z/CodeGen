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

export default observer(() => {
  return (
    <Example>
      <Alert
        cancelButtonText="Cancel"
        confirmButtonText="Confirm"
        icon="trash"
        intent={Intent.DANGER}
        isOpen={gstore.staticOverlay.deleteItem.open}
        loading={gstore.staticOverlay.deleteItem.loading}
        onCancel={() => {
          gstore.staticOverlay.deleteItem.open = false;
        }}
        onConfirm={() => {
          gstore.staticOverlay.deleteItem.confirm();
        }}
      >
        <p>
          Do you want to delete the server{" "}
          <b>{gstore.staticOverlay.deleteItem.name}</b>? This operation is
          irreversible.
        </p>
      </Alert>
    </Example>
  );
});
