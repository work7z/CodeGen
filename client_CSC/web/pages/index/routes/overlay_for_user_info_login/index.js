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
import classNames from "classNames";
import {
  INTENT_PRIMARY,
  INTENT_SUCCESS,
} from "@blueprintjs/core/lib/esm/common/classes";
import DialogCommon from "../dialog_common";
import GFormInput from "../../components/GFormInput";
import GFormSelect from "../../components/GFormSelect";
import GFormFilePathSelect from "../../components/GFormFilePathSelect";
import GFormSwitch from "../../components/GFormSwitch";
import _ from "lodash";

export default observer(() => {
  return (
    <div>
      <DialogCommon
        width="500px"
        noFoot={true}
        obj={gstore.user.overlayForUserInfo}
        jsx={observer(() => {
          return (
            <div>
              <h3>Actions</h3>
              <p>
                <Button
                  intent={"danger"}
                  text="Sign Out"
                  onClick={() => {
                    if (
                      !window.confirm(
                        "Are you sure that you want to sign out this account?"
                      )
                    ) {
                      return;
                    }
                    [
                      "SYS_USER_EMAIL",
                      "SYS_USER_NAME",
                      "USER_TOKEN",
                      "USER_INFO",
                    ].map((x) => {
                      localStorage.removeItem(x);
                    });
                    location.reload();
                  }}
                ></Button>
              </p>
              <h3>Cloud Synchronize Services</h3>
              <p>
                Sorry for the fact that the cloud synchronize service still
                under testing, we will release it as soon as possible.
              </p>
            </div>
          );
        })}
        confirm={gutils.api.user.login}
        confirmDisable={!gstore.user.loginPageData.isAddModelPass}
      />
    </div>
  );
});
