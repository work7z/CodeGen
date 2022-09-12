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
  PanelStack2,
  ButtonGroup,
  TextArea,
  Intent,
  Position,
  Toaster,
  Checkbox,
  NumericInput,
  FormGroup,
  Breadcrumbs,
  HTMLSelect,
  ControlGroup,
  InputGroup,
  Navbar,
  NavbarHeading,
  NonIdealState,
  NavbarDivider,
  Boundary,
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
import _ from "lodash";
import StaticServerAdded from "../static_server_added/index.js";
import StaticServerView from "../static_server_view/index.js";
import ProxyServerAdded from "../proxy_server_added/index.js";
import ProxyServerView from "../proxy_server_view/index.js";
import Database_connections from "../database_connections";

export default observer(() => {
  let hist = useHistory();

  return (
    <div
      className="main-frame-right-box"
      style={{
        width: `calc(100vw - ${gutils.frame_defaultWidth})`,
      }}
    >
      <div className="main-frame-right-bread-wrapper">
        <Breadcrumbs items={gstore.sysinfo.breadmenu || []} />
      </div>
      <div
        className={"main-frame-right-card-wrapper "}
        data-path={hist.location.pathname || "none"}
      >
        <Switch>
          <Route
            exact
            path="/board/server/static/added"
            component={StaticServerAdded}
          ></Route>
          <Route
            exact
            path="/board/server/static/view"
            component={StaticServerView}
          ></Route>
          <Route
            exact
            path="/board/server/proxy/added"
            component={ProxyServerAdded}
          ></Route>
          <Route
            exact
            path="/board/server/proxy/view"
            component={ProxyServerView}
          ></Route>
          <Route
            exact
            path="/board/database/connections"
            component={Database_connections}
          ></Route>
        </Switch>
      </div>
    </div>
  );
});
