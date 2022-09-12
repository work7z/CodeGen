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
  PopoverInteractionKind,
  Radio,
  Popover,
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
import gapi from "./gapi.js";
import React from "react";
import ReactDOM from "react-dom";
import { useState } from "react";
import axios from "axios";
import {
  useStores,
  useAsObservableSource,
  useLocalStore,
  useObserver,
} from "mobx-react-lite";
import { Provider, observer, inject } from "mobx-react";
var createHistory = require("history").createBrowserHistory;
import $ from "jquery";
window.$ = $;
import {
  withRouter,
  HashRouter as Router,
  Switch,
  Route,
  Link,
  useHistory,
} from "react-router-dom";
var { autorun, observable } = require("mobx");
var Moment = require("moment");
import _ from "lodash";
import gstore from "./store.js";

const AppToaster = Toaster.create({
  className: "recipe-toaster",
  position: Position.TOP,
});

window.axios = axios;

function uuid() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
    .replace(/[xy]/g, function (c) {
      var r = (Math.random() * 16) | 0,
        v = c == "x" ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    })
    .replace(/-/gi, "");
}

const renderingStFunc = () => {
  return {
    0: {
      label: "Not Running",
      color: "black",
    },
    1: {
      color: gutils.intent.success,
      label: "Running",
    },
    2: {
      label: "Error Occured",
      color: gutils.intent.danger,
    },
    5: {
      label: "Executing",
    },
  };
};

window.Moment = Moment;

const ruid = window.ipc.ruid;

const cacheMap = {};
const cacheMapForEditor = {};

let gutils = {
  w_alertError(errmsg, conf) {
    return gutils.w_alertMsgGlobal(
      _.merge(
        {
          noCancel: true,
          confirmIntent: "none",
          icon: "error",
          width: "430px",
          s_clzname: "danger-view",
          confirmText: "Close",
          confirmIntent: "none",
          title: "An error occurred...",
          jsx: () => {
            return <div>{errmsg}</div>;
          },
        },
        conf
      )
    );
  },
  w_alertSuccess(errmsg, conf) {
    return gutils.w_alertMsgGlobal(
      _.merge(
        {
          noCancel: true,
          confirmIntent: "none",
          icon: "error",
          width: "430px",
          s_clzname: "succ-view",
          confirmText: "Close",
          confirmIntent: "none",
          title: conf.title || "Positive Operation Result",
          jsx: () => {
            return <div>{errmsg}</div>;
          },
        },
        conf
      )
    );
  },
  w_alertMsgGlobal(obj) {
    const unikey = gutils.uuid();
    _.defaultsDeep(obj, {
      loading: false,
      open: false,
      confirm: null,
      title: "Unknown",
      icon: null,
      cancelText: "Cancel",
      confirmIntent: null,
      confirmText: "Confirm",
    });
    obj.cleanFunc = () => {
      delete gstore.settings.alerts[unikey];
    };
    gstore.settings.alerts[unikey] = {
      ...obj,
      open: true,
    };
    autorun(() => {
      const obj = gstore.settings.alerts[unikey];
      console.log("checking and turn it off", obj, obj.cleanFunc);
      if (obj && obj.open == false) {
        gutils.defer(() => {
          obj.cleanFunc();
        });
      }
    });
    return gstore.settings.alerts[unikey];
  },
  clearCache() {
    _.forEach(gutils.whenBlurFunc, (x) => x());
  },
  delay(fn) {
    return _.debounce(fn, 300);
  },
  anyEmpty(arr) {
    let isAnyEmpty = false;
    for (let value of arr) {
      isAnyEmpty = _.isNil(value) || value == "";
      if (isAnyEmpty) {
        return true;
      }
    }
    return false;
  },
  anyMax(arr) {
    let isAnyEmpty = false;
    for (let value of arr) {
      if (_.isNil(value.value)) {
        continue;
      }
      isAnyEmpty = value.value.length >= value.max;
      if (isAnyEmpty) {
        return true;
      }
    }
    return false;
  },
  empty(str) {
    return _.isNil(str) || _.trim(str + "").length == 0;
  },
  uuid,
  crt_live_id: uuid(),
  drag(dragBody, dragRef) {
    gutils.defer(() => {
      const dialogDIV = dragBody;
      const $header = dragRef;
      window.isDragging = false;
      const handleObj = {
        pageX: null,
        pageY: null,
        padLeft: null,
        padTop: null,
        finLeft: null,
        finTop: null,
      };
      function setFuncForHandle(rect, handleObj) {
        handleObj.finLeft = rect.left + handleObj.padLeft;
        handleObj.finTop = rect.top + handleObj.padTop;
        handleObj.padLeft = 0;
        handleObj.padTop = 0;
      }

      const delayUpdateEle = () => {
        dialogDIV[0].style.top = handleObj.finTop + "px";
        dialogDIV[0].style.left = handleObj.finLeft + "px";
      };

      $header
        .css({
          userSelect: "none",
          cursor: "default",
        })
        .mousedown(function (e) {
          window.isDragging = true;
          const $dialogDIV = $(dialogDIV[0]);

          handleObj.padLeft = parseFloat($dialogDIV.css("margin-left"));
          handleObj.padTop = parseFloat($dialogDIV.css("margin-top"));
          const rect = dialogDIV[0].getBoundingClientRect();

          setFuncForHandle(rect, handleObj);

          dialogDIV[0].style.left = handleObj.finLeft + "px";
          dialogDIV[0].style.top = handleObj.finTop + +"px";
          console.log("mouse down");
          handleObj.pageX = e.pageX;
          handleObj.pageY = e.pageY;
          window.global_handleObj = handleObj;
          window.global_delayUpdateEle = delayUpdateEle;
        })
        .mousemove(function (e) {})
        .mouseup(function () {
          window.isDragging = false;
        });
    });
    // register common
    gutils.defer(() => {
      gutils.once("only_trace_global", () => {
        document.addEventListener("mousemove", (e) => {
          const handleObj = window.global_handleObj;
          const delayUpdateEle = window.global_delayUpdateEle;
          if (window.isDragging) {
            const diffTop = e.pageY - handleObj.pageY;
            const diffLeft = e.pageX - handleObj.pageX;

            console.log("draging", { diffTop, diffLeft });

            handleObj.finLeft += diffLeft;
            handleObj.finTop += diffTop;

            delayUpdateEle();

            handleObj.pageX = e.pageX;
            handleObj.pageY = e.pageY;
            console.log("mouse is dragging", e.pageX, e.pageY);
          } else {
            // console.log("simple moving");
          }
        });
        document.addEventListener("mouseup", () => {
          window.isDragging = false;
        });
      });
    });
  },
  resizeEvent(conf) {
    return {
      defaultSize: _.merge(
        {},
        {
          width: conf.obj[conf.key],
          height: "100%",
        },
        conf.size || {}
      ),
      onResizeStop: (event, direct, refToEle, delta) => {
        gutils.defer(() => {
          conf.obj[conf.key] = refToEle.style.width;
        });
      },
    };
  },
  enableResize() {
    return {
      top: false,
      right: false,
      bottom: false,
      left: false,
      topRight: false,
      bottomRight: false,
      bottomLeft: false,
      topLeft: false,
    };
  },
  enableResizeAllTrue() {
    return {
      top: true,
      right: true,
      bottom: true,
      left: true,
      topRight: true,
      bottomRight: true,
      bottomLeft: true,
      topLeft: true,
    };
  },
  getCentreLink(str) {
    const host = window.ipc.dev
      ? "http://127.0.0.1:8080"
      : "https://codegen.work7z.com";
    return host + str;
  },
  waitInitializeRefFunc: [],
  frame_defaultWidth: "220px",
  createEditor: _.debounce(async function (ele, conf, chkObj = {}) {
    if (chkObj && cacheMapForEditor[chkObj.ID]) {
      let preEle = cacheMapForEditor[chkObj.ID].getDomNode();
      if (!document.body.contains(preEle)) {
        cacheMapForEditor[chkObj.ID].dispose();
      } else {
        return;
      }
    }
    await gutils.getScript("app.bundle.js");
    const { monaco } = window.CodeGenPluginED();
    let inst = monaco.editor.create(ele, conf);
    cacheMapForEditor[chkObj.ID] = inst;
    window.crt_editor = inst;
    if (chkObj.created) {
      chkObj.created(inst);
    }
    return inst;
  }, 30),
  scriptCache: {},
  getScript(str) {
    if (gutils.scriptCache[str] && window.CodeGenPluginED) {
      return;
    }
    gutils.scriptCache[str] = "1";
    str = "../e/" + str;
    return new Promise((ok) => {
      $.getScript(str, () => {
        ok();
      });
    });
  },
  safeparse(str) {
    try {
      return JSON.parse(str);
    } catch (err) {
      return null;
    }
  },
  safeparsenow(str) {
    try {
      return JSON.parse(str);
    } catch (err) {
      return null;
    }
  },
  getSetting(mykey) {
    return gstore.settings.model[mykey];
  },
  col_yesno(obj) {
    return {
      label: obj.label,
      value: (x) => (x[obj.value] == 1 ? "Yes" : "No"),
    };
  },
  filternil(arr) {
    return _.filter(arr, (x) => !_.isNil(x));
  },
  reInitAllDataBeforeOpenModal(obj, alertType, x) {
    obj.alertType = alertType;
    obj.addModelFailures = {};
    obj.isAddModelPass = alertType == "update" ? true : false;
    if (alertType === "update") {
      obj.addModel = gutils.clone(x);
    } else {
      obj.addModel = gutils.clone(obj.initModel);
    }
  },
  validate_http: (x) => {
    return /(http|https):\/\/([\w.]+\/?)\S*/.test(x);
  },
  // llinit
  init() {
    // gutils.api.proxy.openAddingModal("create");
    setTimeout(() => {
      // gutils.api.proxy.openAddRulePanel();
      setTimeout(() => {
        // gutils.api.proxy.openAddRulePathRewritePanel();
      }, 500);
    }, 200);
    setTimeout(() => {
      // gutils.api.system.openSettingAPI("preferences");
      gutils.api.dblink.create_connection();
    }, 300);
    autorun(() => {
      let localSettings = gstore.localSettings;
      console.log("got new chg for ");
      let newSettingObj = {};
      _.forEach(window.RAW_LOCALSETTING, (x, d, n) => {
        newSettingObj[d] = localSettings[d];
      });
      localStorage.setItem("LOCAL_SETTINGS", JSON.stringify(newSettingObj));
    });
    // key register
    document.onkeydown = function (e) {
      if (e.which == 16) {
        window.down16 = true;
      }
      if (e.which == 91) {
        window.down91 = true;
      }
      if (window.down16 && window.down91 && e.which == 48) {
        gstore.localSettings.isLeftMenuOpen =
          !gstore.localSettings.isLeftMenuOpen;
      }
      // console.log("keydown", e.which, e.ctrlKey, e.altKey, e.shiftKey);
    };
    document.onkeyup = function (e) {
      window.down16 = null;
      window.down91 = null;
      // console.log("keyup", e.which, e.ctrlKey, e.altKey, e.shiftKey);
      // if (e.which == 77) {
      //   alert("M key was pressed");
      // } else if (e.ctrlKey && e.which == 66) {
      //   alert("Ctrl + B shortcut combination was pressed");
      // } else if (e.ctrlKey && e.altKey && e.which == 89) {
      //   alert("Ctrl + Alt + Y shortcut combination was pressed");
      // } else if (e.ctrlKey && e.altKey && e.shiftKey && e.which == 85) {
      //   alert("Ctrl + Alt + Shift + U shortcut combination was pressed");
      // }
    };
  },
  jsx_remove(x, props, rowidx) {
    return (
      <a
        href={gutils.void_ref}
        onClick={() => {
          props.onChange(gutils.pickArr(props.value, rowidx));
        }}
      >
        Remove
      </a>
    );
  },
  delete_nouse_id_before_clean(x) {
    const newobjx = gutils.clone(x);
    delete newobjx["ID"];
    let temparr = [
      "EXTRA_DATA_PROXY_RULES_PATH_REWRITE",
      "EXTRA_DATA_PROXY_RULES",
    ];
    temparr.forEach((kkk) => {
      _.forEach(newobjx[kkk], (xxx) => {
        delete xxx["ID"];
        delete xxx["CONFIG_ID"];
        delete xxx["CONFIG_RULE_ID"];
        _.forEach(temparr, (x1) => {
          if (xxx[x1]) {
            _.forEach(xxx[x1], (n) => {
              delete n["ID"];
              delete n["CONFIG_RULE_ID"];
              delete n["CONFIG_ID"];
            });
          }
        });
      });
    });
    return newobjx;
  },
  jsx_duplicate(x, props, rowidx) {
    return (
      <a
        href={gutils.void_ref}
        onClick={() => {
          const newarr = [...props.value];
          const newobjx = gutils.delete_nouse_id_before_clean(x);
          newarr.push(newobjx);
          props.onChange(newarr);
        }}
      >
        Duplicate
      </a>
    );
  },
  col_disable(props) {
    return {
      label: "Status",
      value: (x) => (x.DISABLE == 1 ? "Disabled" : "Enabled"),
    };
  },
  jsx_disable(x, func) {
    return "";
    return (
      <a
        href={gutils.void_ref}
        onClick={() => {
          // x.DISABLE = x.DISABLE == 0 || _.isNil(x.DISABLE) ? 1 : 0;
          func();
        }}
      >
        {x.DISABLE == 0 || _.isNil(x.DISABLE) ? "Disable" : "Enable"}
      </a>
    );
  },
  pickArr(arr, index) {
    return _.concat(_.slice(arr, 0, index), _.slice(arr, index + 1));
  },
  isChinaUser: false,
  hist: null,
  confirm(msg) {
    return window.confirm(msg);
  },
  copy(ctn) {
    var obj = document.getElementById("uniqueiptele");
    obj.value = ctn;
    obj.select();
    document.execCommand("Copy");
  },
  renderingStaticRunStatus: renderingStFunc,
  renderingProxyRunStatus: renderingStFunc,
  runStatusViewColor: () => {
    return {
      0: null,
      1: gutils.intent.danger,
      2: gutils.intent.success,
      5: gutils.intent.fineyellow,
    };
  },
  logging_obj() {
    let redColor = "#ff2020";
    return {
      0: {
        label: "INFO",
        color: "deepskyblue",
      },
      1: {
        label: "SUCC",
        color: "yellowgreen",
      },
      2: {
        label: "ERROR",
        color: redColor,
      },
      3: {
        label: "WARNING",
        color: "yellow",
      },
      4: {
        label: "DEBUG",
        color: "green",
      },
      14: {
        label: "ERROR",
        color: redColor,
      },
    };
  },
  logging_list() {
    return [
      {
        label: "INFO",
        value: 0,
      },
      {
        label: "SUCC(Success)",
        value: 1,
      },
      {
        label: "ERROR",
        value: 2,
      },
      {
        label: "ERROR(DEBUG)",
        value: 14,
      },
      {
        label: "WARNING",
        value: 3,
      },
      {
        label: "DEBUG",
        value: 4,
      },
    ];
  },
  runStatusViewColorWithIntent: () => {
    return {
      0: "primary",
      1: "danger",
      2: "success",
      5: "warning",
    };
  },
  runStatusDefineObj: () => {
    return {
      0: "Start",
      1: "Stop",
      2: "Restart",
      5: "Interrupt",
    };
  },
  deleteConfirmPanel(func) {
    return (
      <div key="text">
        <p>Do you want to trigger the delete operation?</p>
        <div
          style={{ display: "flex", justifyContent: "flex-end", marginTop: 15 }}
        >
          <Button
            className={Classes.POPOVER_DISMISS}
            style={{ marginRight: 10 }}
          >
            Cancel
          </Button>
          <Button
            intent={Intent.DANGER}
            className={Classes.POPOVER_DISMISS}
            onClick={() => {
              func();
            }}
          >
            Delete
          </Button>
        </div>
      </div>
    );
  },
  whenBlurFunc: [],
  commonPopover() {
    return {
      popoverClassName: Classes.POPOVER_CONTENT_SIZING,
      portalClassName: "faults",
      minimal: true,
      captureDismiss: true,
      interactionKind: PopoverInteractionKind.HOVER_TARGET_ONLY,
    };
  },
  sleep(val) {
    return new Promise((e) => {
      setTimeout(() => {
        e();
      }, val);
    });
  },
  clone(obj) {
    return _.cloneDeep(obj);
  },
  propsForInput(props, iptVal, onIptVal) {
    console.log("input value", props.id, props.value);
    return {
      intent: props.intent,
      placeholder: props.placeholder,
      id: props.id,
      value: props.value,
      onBlur: (x) => {
        // console.log("final update", iptVal);
        // props.onChange(iptVal);
        _.forEach(gutils.whenBlurFunc, (x, d, n) => {
          x();
        });
        gutils.whenBlurFunc = [];
      },
      asyncControl: true,
      onChange: (x) => {
        // console.log("on change", x.target.value);
        // onIptVal(x.target.value);
        props.onChangeDelay(x.target.value, {
          // needUpdateValue: false,
        });
        if (props.chg) {
          props.chg(x.target.value);
        }
      },
    };
  },
  wakeobj: {},
  selectDir(func) {
    window.ipc.receiveOnce("get-dirs", (value) => {
      console.log("selectDirs", value);
      func(_.first(value));
    });
    window.ipc.send("select-dirs");
  },
  createForm(modelObj, modelKeys, arr) {
    const model = modelObj[modelKeys.model];
    const modelFailures = modelObj[modelKeys.failures];
    function validateCurrentX(x, finval) {
      let isDanger = false,
        dangerTooltip = null;
      if (
        x.need &&
        (_.isNil(finval) ||
          (_.isString(finval) && finval.length == 0) ||
          (finval.length == 0 && _.isArray(finval)))
      ) {
        isDanger = true;
        dangerTooltip = "Value cannot be empty";
      }
      let strval = "" + (_.isNil(finval) ? "" : finval);
      if (x.max && strval.length >= x.max) {
        isDanger = true;
        dangerTooltip = "Value length cannot greater than maximum " + x.max;
      }
      if (x.validator) {
        let isok = x.validator(finval);
        if (!isok) {
          isDanger = true;
          dangerTooltip =
            x.errorText ||
            "The formatting of value mismatches with the validate rule.";
        }
      }

      return {
        isDanger,
        dangerTooltip,
      };
    }
    return (
      <Example>
        {_.chain(arr)
          .map((x, d, n) => {
            let mycrtid = x.prop;
            const CrtTag = x.jsx;
            let extraFormGroupsProps = {
              helperText: x.tooltip,
            };
            if (_.isNil(model[x.prop]) && x.defaultValue) {
              gutils.defer(() => {
                model[x.prop] = x.defaultValue;
              });
            }
            const crtValidObj = modelFailures[x.prop];
            if (crtValidObj) {
              extraFormGroupsProps.intent = "danger";
              extraFormGroupsProps.helperText = crtValidObj.dangerTooltip;
            }
            function checkTotalValid() {
              return _.chain(arr)
                .filter((xx) => xx.prop != x.prop)
                .map((x) => validateCurrentX(x, model[x.prop]))
                .thru((x) => {
                  for (let e of x) {
                    if (e && e.isDanger) {
                      return false;
                    }
                  }
                  return true;
                })
                .value();
            }
            const updateFuncMiles = 50;
            const debounce = (x) => x;
            const deleteModelFailuresFunc = debounce(() => {
              delete modelFailures[x.prop];
            }, 500);
            const updateAllPassFunc = debounce((isAllPass) => {
              modelObj[modelKeys.isAllPass] = isAllPass;
            }, 500);
            const updateFuncDelay = debounce((finval) => {
              model[x.prop] = finval;
            }, updateFuncMiles);
            const chgfunc = (temp1, configForChg = {}) => {
              _.defaultsDeep(configForChg, {
                needUpdateValue: true,
              });
              let finval = temp1;
              if (
                _.get(temp1, "__proto__.constructor.name") === "SyntheticEvent"
              ) {
                finval = temp1.target.value;
              }
              const { dangerTooltip, isDanger } = validateCurrentX(x, finval);

              let isAllPass = null;
              if (isDanger) {
                isAllPass = false;
              } else {
                isAllPass = checkTotalValid();
              }
              if (isAllPass != null) {
                gutils.whenBlurFunc.push(() => {
                  updateAllPassFunc(isAllPass);
                });
              }
              if (isAllPass == false && modelObj[modelKeys.isAllPass] == true) {
                updateAllPassFunc(false);
              }

              if (!isDanger && modelFailures[x.prop]) {
                deleteModelFailuresFunc();
              }
              if (isDanger) {
                modelFailures[x.prop] = {
                  dangerTooltip,
                };
              }
              console.log("changing", finval, x);
              if (configForChg.needUpdateValue) {
                updateFuncDelay(finval);
              }
            };
            const put_value = model[x.prop];
            const put_onchg = (...args) => {
              try {
                return chgfunc(...args);
              } catch (e) {
                debugger;
                console.error("fail", e);
                throw e;
              }
            };
            gutils.wakeobj[modelKeys.wakekey + "." + mycrtid] = () => {
              gutils.defer(() => {
                put_onchg(model[x.prop]);
              });
            };
            if (x.type == "html") {
              return (
                <div
                  key={x.prop}
                  style={{
                    margin: "0 0 15px",
                  }}
                >
                  {x.value({
                    value: put_value,
                    onChange: put_onchg,
                  })}
                </div>
              );
            }
            const showFunc = x.show;
            if (showFunc) {
              if (!showFunc(model)) {
                return null;
              }
            }
            return (
              <FormGroup
                {...extraFormGroupsProps}
                label={x.label}
                labelFor={mycrtid}
                labelInfo={x.need ? "(required)" : ""}
                key={x.prop}
              >
                <CrtTag
                  intent={extraFormGroupsProps.intent}
                  id={mycrtid}
                  placeholder={x.placeholder}
                  value={put_value}
                  onChange={put_onchg}
                  onChangeDelay={debounce(chgfunc, 300)}
                />
              </FormGroup>
            );
          })
          .filter((x) => !_.isNil(x))
          .value()}
      </Example>
    );
  },
  alertOk(config) {
    if (_.isString(config)) {
      config = {
        message: config,
      };
    }
    gutils.alert({
      intent: "success",
      icon: "endorsed",
      ...config,
    });
  },
  alert(config) {
    if (_.isString(config)) {
      config = {
        message: config,
      };
    }
    AppToaster.show(config);
  },
  alertOkDirect(obj) {
    window.alert(obj.message);
  },
  genCol(arr, tableData = []) {
    return _.chain(arr)
      .map((x, d, n) => {
        return {
          name: x.label,
          render(rowIndex) {
            let crtRowObj = tableData[rowIndex];
            let finValue = "";
            if (!_.isNil(crtRowObj)) {
              if (_.isString(x.value)) {
                finValue = crtRowObj[x.value];
              } else {
                finValue = x.value(crtRowObj, rowIndex);
              }
            }
            return <Cell key={x.label}>{finValue}</Cell>;
          },
        };
      })
      .value();
  },
  noNaN(val) {
    return isNaN(val) ? 0 : val;
  },
  noNaNWithNull(val) {
    return isNaN(val) ? null : val;
  },
  removeOnce(key) {
    delete cacheMap[key];
  },
  once(key, func) {
    if (cacheMap[key]) {
      return;
    }
    cacheMap[key] = 1;
    func();
  },
  api: gapi,
  app_name: "CodeGen",
  app_version: "1.1.0",
  sample_text: "the quick brown fox jumps over the lazy dog",
  iterateTree(arr, loopFunc, nextKey = "children") {
    return _.map(arr, (x, d, n) => {
      loopFunc(x, d, n);
      if (x[nextKey]) {
        x[nextKey] = gutils.iterateTree(x[nextKey], loopFunc, nextKey);
      }
      return x;
    });
  },
  intent: {
    danger: "#db3737",
    success: "#0f9960",
    fineyellow: "#a71fa9",
  },
  void_ref: "javascript:void(0);",
  formatDate(date) {
    return new Moment(date).format("YYYY-MM-DD HH:mm:ss");
  },
  findTree(arr, loopFunc, findPushArr = []) {
    let nextKey = "children";
    let findObj = null;
    _.every(arr, (x, d, n) => {
      findObj = loopFunc(x, d, n);
      if (x[nextKey] && findObj == null) {
        findObj = gutils.findTree(x[nextKey], loopFunc, findPushArr);
      }
      if (findObj != null) {
        findPushArr.push(x);
      }
      return findObj == null;
    });
    return findObj;
  },
  defer(func, timeout = 0) {
    setTimeout(func, timeout);
  },
  optCentre(url, param = {}, confg = {}) {
    let finURL = gutils.getCentreLink(url);
    return new Promise((okfunc, errfunc) => {
      axios({
        method: "POST",
        url: finURL,
        data: param,
        headers: {
          "Content-Type": "application/json",
          "X-WORK7Z-RES-TYPE": "JSON",
          "X-FE-RUID": ruid,
          "X-WORK7Z-DEV": "MOB",
          "X-WORK7Z-MACHINEID": window.ipc.getMachineId(),
        },
        validateStatus: (status) => {
          return true; // I'm always returning true, you may want to do it depending on the status received
        },
      })
        .then((e) => {
          console.log("got then ", e);
          if (e.status != 200) {
            if (e.data && e.data.message && !config.mute) {
              gutils.alert({
                intent: "danger",
                icon: "error",
                message: e.data.message,
              });
            }
            errfunc(e);
          } else {
            e.data["TOKEN"] = e.headers["x-work7z-token"];
            if (e.headers["x-work7z-username"]) {
              localStorage.setItem(
                "SYS_USER_NAME",
                e.headers["x-work7z-username"]
              );
            }
            if (e.headers["x-work7z-email"]) {
              localStorage.setItem(
                "SYS_USER_EMAIL",
                e.headers["x-work7z-email"]
              );
            }
            okfunc(e.data);
          }
        })
        .catch((re, val) => {
          console.log("got failure", re, val);
          errfunc(re);
        });
    });
  },
  opt(url, param, config) {
    const conf = {
      method: "POST",
      data: {
        param: param,
      },
      headers: {
        "Content-Type": "application/json",
        "X-FE-RUID": ruid,
        "X-FE-LIVE-ID": gutils.crt_live_id,
      },
    };
    return new Promise((okfunc, errfunc) => {
      const port = _.get(config, "port", window.ipc.port);
      const completeURL = `http://127.0.0.1:${port}${url}`;
      const CancelToken = axios.CancelToken;
      const source = CancelToken.source();
      if (config && config.ref) {
        config.ref(source);
      }
      axios({
        ...conf,
        cancelToken: source.token,
        url: completeURL,
        validateStatus: (status) => {
          return true; // I'm always returning true, you may want to do it depending on the status received
        },
      })
        .then((e) => {
          console.log("got then ", e);
          if (e.status != 200) {
            if (
              e.data &&
              e.data.message &&
              completeURL.indexOf("waiting-for") == -1 &&
              param.errorResponse != true
            ) {
              gutils.alert({
                intent: "danger",
                icon: "error",
                message: e.data.message,
              });
            }
            errfunc(e);
          } else {
            okfunc(e.data);
          }
        })
        .catch((re, val) => {
          console.log("got failure", re, val);
          errfunc(re);
        });
    });
  },
};
window.gutils = gutils;

export default gutils;
