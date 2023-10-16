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
  PopoverPosition,
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
  Popover,
  PopoverInteractionKind,
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
import gapi from "../../gapi";
import { INTENT_PRIMARY } from "@blueprintjs/core/lib/esm/common/classes";
import NotificationPanel from "../NotificationPanel/index";

export default observer(() => {
  const noMsgNow = gstore.sysinfo.latestNewMsgCount == 0;
  const hist = useHistory();
  gutils.hist = hist;
  gutils.once("run listen history", () => {
    hist.listen((e) => {
      console.log("updating listen history", e, gstore.sysinfo);
      gstore.sysinfo.latestRoutePath = e.pathname;
    });
  });
  gutils.once("testrunning", () => {
    gutils.init();
  });
  let expandMenu = (
    <Button
      text=""
      className={Classes.MINIMAL}
      rightIcon={gstore.localSettings.isLeftMenuOpen ? "menu" : "menu"}
      onClick={() => {
        gstore.localSettings.isLeftMenuOpen =
          !gstore.localSettings.isLeftMenuOpen;
      }}
    ></Button>
  );
  const isLogin = !_.isNil(localStorage.getItem("USER_TOKEN"));
  return (
    <div>
      <Navbar>
        {false ? (
          ""
        ) : (
          <NavbarGroup align={Alignment.LEFT}>
            {expandMenu}
            <Button
              className={Classes.MINIMAL}
              icon="user"
              text={
                isLogin
                  ? localStorage.getItem("SYS_USER_NAME")
                  : "Synchronize via Cloud"
              }
              intent="primary"
              onClick={() => {
                if (isLogin) {
                  gstore.user.overlayForUserInfo.open = true;
                } else {
                  gstore.user.overlayForLogin.open = true;
                }
              }}
            ></Button>
          </NavbarGroup>
        )}
        <NavbarGroup align={Alignment.RIGHT}>
          {/* <Button
            className={Classes.MINIMAL}
            text="Services"
            icon="helper-management"
          /> */}
          <Popover
            popoverClassName={Classes.POPOVER_WRAPPER}
            portalClassName="faults"
            interactionKind={PopoverInteractionKind.CLICK_TARGET_ONLY}
            enforceFocus={false}
            captureDismiss={true}
            position={PopoverPosition.BOTTOM_RIGHT}
            isOpen={gstore.sysinfo.isOpenNotification}
          >
            <Button
              className={Classes.MINIMAL}
              onClick={() => {
                gapi.msg.openNotificationPanel();
              }}
              text={
                "Notifications" +
                (noMsgNow ? "" : `(${gstore.sysinfo.latestNewMsgCount})`)
              }
              intent={noMsgNow ? null : "primary"}
              icon="notifications"
            />
            <div>
              <NotificationPanel />
            </div>
          </Popover>
          <Button
            className={Classes.MINIMAL}
            onClick={() => {
              gutils.api.system.openSettingAPI("general");
            }}
            text="Settings"
            icon="cog"
          />
          <Button
            className={Classes.MINIMAL}
            text="About Software"
            icon="helper-management"
            onClick={() => {
              gutils.api.system.openSettingAPI("abouts");
            }}
          />
        </NavbarGroup>
      </Navbar>
    </div>
  );
});
