import {
  Callout,
  PanelStack,
  ProgressBar,
  AnchorButton,
  Tooltip,
  Dialog,
  Drawer,
  Overlay,
  ResizeSensor,
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
import { useState, useContext } from "react";
import {
  useStores,
  useSelector,
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
import GTree from "../../components/tree/index.js";
import _ from "lodash";
import { Resizable } from "re-resizable";
const style = {
  // display: "flex",
  // alignItems: "center",
  // justifyContent: "center",
  // border: "solid 1px #ddd",
  // background: "#f0f0f0",
};

export default observer(() => {
  const [defaultW, onDefaultWChg] = useState(
    gstore.localSettings.leftMenuWidth
  );
  // console.log(
  //   "rendering default w",
  //   defaultW,
  //   gstore.localSettings.leftMenuWidth
  // );
  return (
    <Resizable
      enable={_.merge(gutils.enableResize(), {
        right: true,
      })}
      style={style}
      defaultSize={{
        width: defaultW,
        height: "calc(100vh - 50px)",
      }}
      minWidth={gutils.frame_defaultWidth}
      onResizeStop={(event, direct, refToEle, delta) => {
        console.log("onResizeStop", event, direct, refToEle, delta);
        gutils.defer(() => {
          gstore.localSettings.leftMenuWidth = refToEle.style.width;
        });
      }}
      className="main-frame-left-box"
    >
      <div className="system-navigator">
        <span class="appname">
          {gutils.app_name} {gutils.app_version}
        </span>
        <Button
          icon="chevron-left"
          onClick={() => {
            gstore.localSettings.isLeftMenuOpen = false;
          }}
          outlined={true}
        ></Button>
      </div>
      <GTree nodes={gstore.roadmap} />
    </Resizable>
  );
});
