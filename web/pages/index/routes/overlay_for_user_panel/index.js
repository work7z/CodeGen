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
  const validConditions = [
    {
      label: "Username",
      prop: "username",
      need: true,
      max: 100,
      placeholder: "Username or Email",
      // tooltip: "The value will be used as the name of the static server.",
      jsx: (props) => {
        return <GFormInput {...props} />;
      },
    },
    {
      label: "Password",
      prop: "password",
      need: true,
      max: 500,
      placeholder: "Password or SecureKey",
      // tooltip: "The value will be used as the name of the static server.",
      jsx: (props) => {
        return <GFormInput type="password" {...props} />;
      },
    },
    {
      type: "html",
      prop: "EXTRA_TMP_LOGIC_expand_2",
      value: (x) => {
        return (
          <div style={{ textAlign: "right" }}>
            <a
              style={{ marginRight: "15px" }}
              href={gutils.void_ref}
              onClick={() => {
                window.open(gutils.getCentreLink("/user/sign-up"));
              }}
            >
              Create Account
            </a>
            <a
              href={gutils.void_ref}
              onClick={() => {
                window.open(gutils.getCentreLink("/user/find-my-password"));
              }}
            >
              Forgot Password?
            </a>
          </div>
        );
      },
    },
    {
      label: "Image Verification Code",
      prop: "verificationCode",
      need: true,
      max: 500,
      placeholder: "Please input the code which is shown the image below",
      // tooltip: "The value will be used as the name of the static server.",
      jsx: (props) => {
        return <GFormInput {...props} />;
      },
    },
    {
      type: "html",
      prop: "EXTRA_TMP_LOGIC_vcode",
      defaultValue: Math.random(),
      value: (x) => {
        return (
          <div style={{ float: "left", textAlign: "right" }}>
            <div>
              {" "}
              <img
                src={gutils.getCentreLink(
                  "/blob/verify-code/get?timestamp=" + x.value
                )}
              />
            </div>
            <div>
              <a
                href="javascript:void(0);"
                onClick={() => {
                  x.onChange(Math.random());
                }}
              >
                Refresh Code
              </a>
            </div>
          </div>
        );
      },
    },
  ];
  return (
    <div>
      <DialogCommon
        width="500px"
        obj={gstore.user.overlayForLogin}
        jsx={observer(() => {
          return gutils.createForm(
            gstore.user.loginPageData,
            {
              model: "addModel",
              failures: "addModelFailures",
              isAllPass: "isAddModelPass",
            },
            validConditions
          );
        })}
        confirm={gutils.api.user.login}
        confirmDisable={!gstore.user.loginPageData.isAddModelPass}
      />
    </div>
  );
});
