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
import gutils from "./utils";
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
import gstore from "./store.js";

export default () => {
  // latest route info
  autorun(() => {
    const roadmap = gstore.roadmap;
    const latestRoutePath = gstore.sysinfo.latestRoutePath;
    console.log("handle latest route info", latestRoutePath);

    const hist = gutils.hist;
    if (hist) {
      let stackPipeList = [];
      gutils.findTree(
        gstore.roadmap,
        (x) => {
          console.log("checking", hist.location.pathname, x.pathname);
          return hist.location.pathname == x.pathname && x.pathname ? x : null;
        },
        stackPipeList
      );
      const menu = [
        ..._.chain(stackPipeList || [])
          .reverse()
          .map((x, d, n) => {
            let obj = {
              icon: x.icon,
              text: x.label,
              href: x.pathname ? "#" + x.pathname : gutils.void_ref,
              current: _.size(n) === d + 1,
            };
            console.log(obj);
            return obj;
          })
          .thru((x) => [
            {
              text: gutils.app_name,
              icon: "page-layout",
              href: gutils.void_ref,
            },
            ...x,
          ])
          .value(),
      ];
      gstore.sysinfo.breadmenu = menu;
    }
  });
};
