import {
  Callout,
  PanelStack,
  ProgressBar,
  AnchorButton,
  Tooltip,
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
  Dialog,
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
import $ from "jquery";
import { Resizable } from "re-resizable";
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
import { Rnd } from "react-rnd";
// import Draggable from "react-draggable"; // The default
// import { DraggableCore } from "react-draggable"; // <DraggableCore>
// import Draggable, { DraggableCore } from "react-draggable"; // Both at the same time

// window.Rnd = Rnd;
window.Resizable = Resizable;
window._ = _;
// window.Draggable = Draggable;

export default observer((props) => {
  const obj = props.obj;
  const classes = classNames(Classes.CARD, Classes.ELEVATION_4);

  function DialogFooter(x) {
    return (
      <div className={Classes.DIALOG_FOOTER}>
        <div className={Classes.DIALOG_FOOTER_ACTIONS}>
          {props.leftContent}
          {props.left2Content}
          {props.obj.noCancel ? (
            ""
          ) : (
            <Button
              onClick={() => {
                obj.open = false;
                if (obj.onCancel) {
                  obj.onCancel();
                }
              }}
            >
              {obj.cancelText || "Cancel"}
            </Button>
          )}
          <Button
            onClick={() => {
              if (props.confirm) {
                props.confirm();
              }
              if (obj.onConfirm) {
                obj.onConfirm();
              }
            }}
            loading={obj.loading}
            intent={obj.confirmIntent}
            disabled={props.confirmDisable}
          >
            {obj.confirmText}
          </Button>
        </div>
      </div>
    );
  }
  function DialogBody() {
    const MyJSX = props.jsx;
    return (
      <div className={Classes.DIALOG_BODY}>
        <MyJSX />
      </div>
    );
  }
  console.log("dialog common", props);

  const [style, onStyle] = useState({
    left: "50%",
    transform: "translateX(-50%)",
  });
  const [state, onState] = useState({});
  const extraProps =
    props.noBackdrop == true
      ? {
          backdropProps: {
            style: {
              display: "none",
            },
          },
        }
      : {};
  console.log("dialog_common", props);
  return (
    <Example>
      <Dialog
        {...extraProps}
        // canEscapeKeyClose={props.noBackdrop != true}
        portalClassName={props.portalClz}
        className={"mytesting " + props.clzname + " " + props.obj.s_clzname}
        style={{
          ...(props.style || {}),
          width: props.width || "580px",
          zIndex: props.zIndex,
          style,
        }}
        title={props.obj.title}
        isOpen={props.obj.open}
        icon={props.obj.icon}
        onClose={() => {
          props.obj.open = false;
        }}
        mSize={{ width: state.width, height: state.height }}
        mOnResizeStop={(e, direction, ref, delta, position) => {
          onState(
            _.merge({}, state, {
              width: ref.style.width,
              height: ref.style.height,
              ...position,
            })
          );
        }}
        ref={(e) => {
          console.log("got ref for dialog", e);
          window.dialog_ref = e;
          if (e == null) {
            return;
          }
          gutils.defer(() => {
            let { titleId } = e;
            const dialogDIV = $(`[aria-labelledby="${titleId}"]`);
            const $header = dialogDIV.find(".bp3-dialog-header");
            gutils.drag(dialogDIV, $header);
          });
        }}
      >
        <DialogBody />
        {props.noFoot ? "" : <DialogFooter></DialogFooter>}
      </Dialog>
    </Example>
  );
});

/**
 ref={(e) => {
  // e &&
  //   gutils.defer(() => {
  //     console.log("got dialog common", e);
  //     let crtTitleID = e.titleId;
  //     if (crtTitleID) {
  //       let tempTitle = $("#" + crtTitleID);
  //       if (tempTitle) {
  //         let parentObj = $("#" + crtTitleID).parent(".bp3-portal");
  //         debugger;
  //         if (parentObj) {
  //           parentObj.css({
  //             zIndex: props.zIndex,
  //           });
  //         }
  //       }
  //     }
  //   });
}}
 */
