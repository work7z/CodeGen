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

const contentSizing = {
  popoverProps: { popoverClassName: Popover2Classes.POPOVER2_CONTENT_SIZING },
};

export default observer((props) => {
  const { nodes = [] } = props;
  let hist = useHistory();

  let nodeClick = (x) => {
    console.log("node click", x);
    if (!_.isEmpty(x.rawobj.children)) {
      x.rawobj.expand = !x.rawobj.expand;
    } else {
      // gutils.iterateTree(nodes, (x) => (x.select = false));
      // x.rawobj.select = true;
      let goPath = x.rawobj.pathname;
      hist.push(goPath);
    }
  };

  const latestRoutePath = gstore.sysinfo.latestRoutePath;

  let formattingFunc = (nodesArr = [], len = 0, hist, tmpCache = {}) => {
    return _.chain(nodesArr)
      .map((x) => {
        len++;
        const haschild = !_.isEmpty(x.children);
        let isCurrentActive = hist.location.pathname == x.pathname;
        let crtobj = {
          rawobj: x,
          id: len,
          icon: x.icon ? x.icon : haschild ? "folder-close" : "application",
          isExpanded: x.expand,
          // isSelected: (x.select || isCurrentActive) && !haschild,
          isSelected: latestRoutePath != null && x.pathname == latestRoutePath,
          hasCaret: haschild,
          label: (
            <ContextMenu2 {...contentSizing} content={<div>Hello there!</div>}>
              <Tooltip2 content={x.desc} placement="right">
                {x.label}
              </Tooltip2>
            </ContextMenu2>
          ),
          childNodes: formattingFunc(
            _.chain(x.children).value(),
            len + 1,
            hist
          ),
        };
        if (!x.select && isCurrentActive) {
        }
        return crtobj;
      })
      .value();
  };

  const formattedNodes = formattingFunc(nodes, 1, hist);

  return (
    <Tree
      contents={formattedNodes}
      onNodeClick={nodeClick}
      onNodeCollapse={(x) => {
        x.rawobj.expand = false;
      }}
      onNodeExpand={(x) => {
        x.rawobj.expand = true;
      }}
      className={Classes.ELEVATION_0}
    />
  );
});
