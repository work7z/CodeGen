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
  Tabs,
  Tab,
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
import Settings_preferences from "../settings_preferences";
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
import classNames from "classNames";
import {
  INTENT_PRIMARY,
  INTENT_SUCCESS,
} from "@blueprintjs/core/lib/esm/common/classes";
import DialogCommon from "../dialog_common";

export default observer(() => {
  const drawerConfig = gstore.settings.drawerConfig;
  const state = {
    autoFocus: true,
    canEscapeKeyClose: true,
    canOutsideClickClose: true,
    enforceFocus: true,
    hasBackdrop: true,
    isOpen: drawerConfig.open,
    size: "70%",
    position: Position.RIGHT,
    usePortal: true,
    title: "System Settings",
  };
  const DefaultPanel = () => {
    return <div>haven't supported yet</div>;
  };
  return (
    <div>
      <Drawer
        className={"mydrawerclz"}
        icon="cog"
        onClose={() => {
          drawerConfig.open = false;
        }}
        {...state}
      >
        <div
          className={Classes.DRAWER_BODY}
          style={{
            padding: "10px",
          }}
        >
          <Card
            style={{
              minHeight: "520px",
            }}
          >
            <Example className="docs-tabs-example">
              <Tabs
                animate={true}
                key={"vertical"}
                renderActiveTabPanelOnly={true}
                vertical={true}
                onChange={(val) => {
                  drawerConfig.view_key = val;
                }}
                large={false}
                selectedTabId={drawerConfig.view_key}
              >
                <Tab id="general" title="General" panel={<DefaultPanel />} />
                <Tab id="network" title="Network" panel={<DefaultPanel />} />
                <Tab
                  id="preferences"
                  title="Preferences"
                  panel={<Settings_preferences />}
                />
                <Tab
                  id="updates"
                  title="Software Updates"
                  panel={<DefaultPanel />}
                />
                <Tab
                  id="abouts"
                  title={"About the Software"}
                  panel={<DefaultPanel />}
                />
              </Tabs>
            </Example>
          </Card>
        </div>
        <div className={Classes.DRAWER_FOOTER}>
          <Button
            text="Restore"
            intent="danger"
            loading={gstore.settings.loading}
            onClick={() => {
              gutils.api.system.restoreAllSettings();
            }}
          ></Button>
          <div
            style={{
              float: "right",
            }}
          >
            <Button
              text="Cancel"
              onClick={() => {
                drawerConfig.open = false;
              }}
              style={{ marginRight: "10px" }}
            ></Button>
            <Button
              text="Apply"
              loading={gstore.settings.loading}
              intent="primary"
              onClick={() => {
                gutils.api.system.confirmAndUpdateSetting();
              }}
              disabled={!gstore.settings.other.isAddModelPass}
            ></Button>
          </div>
        </div>
      </Drawer>
    </div>
  );
});
