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
import _ from "lodash";
import AlertForStaticDelete from "../alert_for_static_delete/index";
import AlertForProxyDelete from "../alert_for_proxy_delete/index";
import OverlayForAddStaticServer from "../overlay_for_add_static_server/index";
import OverlayForAddProxyServer from "../overlay_for_add_proxy_server/index";
import Overlay_for_add_proxy_rule from "../overlay_for_add_proxy_rule";
import Overlay_for_add_proxy_rule_path_rewrite from "../overlay_for_add_proxy_rule_path_rewrite";
import Drawer_for_settings from "../drawer_for_settings";
import Overlay_for_user_panel from "../overlay_for_user_panel/index";
import Overlay_for_user_info_login from "../overlay_for_user_info_login/index";
import Overlay_for_add_new_conn from "../overlay_for_add_new_conn/index";
import Overlay_for_add_new_folder from "../overlay_for_add_new_folder/index";
import Overlay_for_alertaction from "../overlay_for_alertaction";

export default observer(() => {
  return (
    <div>
      <AlertForStaticDelete />
      <AlertForProxyDelete />
      <OverlayForAddStaticServer />
      <OverlayForAddProxyServer />
      <Overlay_for_add_proxy_rule />
      <Overlay_for_add_proxy_rule_path_rewrite />
      <Drawer_for_settings />
      <Overlay_for_user_panel />
      <Overlay_for_user_info_login />
      <Overlay_for_add_new_conn />
      <Overlay_for_add_new_folder />
      <Overlay_for_alertaction />
    </div>
  );
});
