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
  Spinner,
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

export default observer((props) => {
  let [isCrtLoading = 1, onCrtLoading] = useState(1);
  const crtId = props.id;
  return (
    <div
      id={crtId}
      ref={(e) => {
        if (e != null) {
          gutils.defer(async () => {
            let initEle = document.getElementById(crtId);
            if (initEle != null) {
              let conf = {
                // value: ["SELECT * FROM USER;"].join("\n"),
                value: "",
                minimap: {
                  enabled: false,
                },
                automaticLayout: true,
                language: "mysql",
              };
              if (true) {
                // return;
              }
              gutils.defer(() => {
                gutils.createEditor(initEle, conf, {
                  ID: crtId,
                  created(editor) {
                    console.log("global editor", editor);
                    if (!_.isNil(editor)) {
                      window.db_editor = editor;
                    }
                    onCrtLoading(0);
                  },
                });
              });
            }
          });
        }
      }}
      style={_.merge(props.style, {
        position: "relative",
      })}
      className="sys-editor"
    >
      {isCrtLoading == 1 ? (
        <Example>
          <ProgressBar intent={"none"} size={50} value={null} />
        </Example>
      ) : (
        ""
      )}
    </div>
  );
});
