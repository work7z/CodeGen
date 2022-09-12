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
  PanelStack2,
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
import GFormSelect from "../../components/GFormSelect";
import _ from "lodash";

const stackMap = _.mapValues(
  {
    prelude: {
      props: {},
      renderPanel: (props) => (
        <div className="preli-all-wrap">
          <div className="welcome-preli preli-wrap">
            <h1>Welcome to use {gutils.app_name}</h1>
            <p className="welcome-brief-preli">
              Before using {gutils.app_name}, we need to check and download
              necessary dependencies by your network on this PC to ensure that
              all of these dependencies not having missed while you're using the
              software. Please kindly make sure that the PC can access the{" "}
              <b>Internet</b>, the progress preliminary will not spend too much
              time, thanks.
            </p>
            <p>
              <h3>We guarantee you solemnly for the following items</h3>
              <ul>
                <li>
                  {gutils.app_name} WILL NOT analyse or upload your personal
                  files on this PC.
                </li>
                <li>
                  {gutils.app_name} WILL NOT launch a cyber attack by using this
                  PC and the network.
                </li>
                <li>
                  {gutils.app_name} WILL NOT monitor your user action or data
                  while using this software.
                </li>
                <li>
                  {gutils.app_name} WILL put your SECURITY, PRIVACY, INTEREST
                  first, it's a secure, high efficiency, and offline-able
                  software.
                </li>
                <li>
                  For any kinds of software issues or suggestions, you could
                  contact the Developer via email{" "}
                  <a target="_blank" href="mailto:work7z@outlook.com">
                    work7z@outlook.com
                  </a>
                </li>
              </ul>
            </p>
            <p>
              <h3>Choose your Languages before starting</h3>
              <FormGroup
                label="System Languages"
                labelFor="System Languages"
                labelInfo=""
              >
                <GFormSelect
                  list={gstore.preliAllData.formList.lang}
                  onChange={(e) => {
                    gstore.preliAllData.configs.lang = e.target.value;
                  }}
                  value={gstore.preliAllData.configs.lang}
                />
              </FormGroup>
            </p>
          </div>
          <div className="preli-footer">
            <Button
              intent="none"
              onClick={() => {
                props.openPanel(stackMap.mirrorSetting);
              }}
            >
              Continue
            </Button>
          </div>
        </div>
      ),
      title: "Preliminary",
    },
    mirrorSetting: {
      props: {},
      renderPanel: (props) => {
        const { checkingStatus, mirrors } = useLocalStore(() => {
          return {
            checkingStatus: gstore.preliAllData.checkingStatus,
            mirrors: gstore.preliAllData.formList.mirrors,
          };
        });
        const logsArr = [
          checkingStatus.logs.init,
          checkingStatus.logs.runtime,
          checkingStatus.logs.core,
          checkingStatus.logs.local,
        ];
        let isAnyError = false;
        _.forEach(logsArr, (x, d, n) => {
          if (x.error) {
            isAnyError = true;
          }
        });
        const finLogList = [];
        logsArr.map((x, d, n) => {
          if (!x.work) {
            return "";
          }
          _.forEach(x.msg, (eachMsgItem) => {
            let finval = eachMsgItem.fullText;
            if (_.isNil(finval)) {
              finval =
                (eachMsgItem.ok ? "Downloaded" : "Downloading") +
                " " +
                " the " +
                eachMsgItem.label +
                (eachMsgItem.ok ? " Successfully" : "") +
                (eachMsgItem.ok
                  ? ""
                  : !eachMsgItem.e
                  ? ""
                  : ", status: " +
                    `${eachMsgItem.e.rate}(${eachMsgItem.e.loaded}/${eachMsgItem.e.total})`);
            }
            finLogList.push(
              <li>
                <div style={{ color: eachMsgItem.ok ? "green" : "" }}>
                  {finval}
                </div>
              </li>
            );
          });
          if (x.error) {
            finLogList.push(
              x.error ? (
                <div style={{ color: "#F55656" }}>
                  {x.error.label}, cause info: <b>{x.error.cause}</b>
                </div>
              ) : (
                ""
              )
            );
          }
        });
        return (
          <div className="preli-all-wrap">
            <div className="preli-wrap">
              <p>
                <h3>Download Settings</h3>
                <FormGroup label="Download Mirror" labelInfo="">
                  <GFormSelect
                    list={mirrors}
                    onChange={(e) => {
                      gstore.preliAllData.configs.mirror = e.target.value;
                    }}
                    value={gstore.preliAllData.configs.mirror}
                  />
                </FormGroup>
              </p>
              <p>
                <h3>Download Status</h3>
                {!checkingStatus.start ? (
                  <ul>
                    {" "}
                    <li>Please click "Downloads" button to start.</li>
                  </ul>
                ) : (
                  <ul>
                    {finLogList}
                    {isAnyError ? (
                      <div
                        style={{
                          color: "#0E5A8A",
                          marginTop: "26px",
                        }}
                      >
                        Sorry, cannot handle this progress because of an error,
                        please check the following items and re-try again.
                        <ul>
                          <li>Check if the network is normal.</li>
                          <li>Check if the disk has enough space.</li>
                          <li>
                            Check if {gutils.app_name} was allowed to download
                            files.
                          </li>
                          <li>
                            Check if another software interfere this action.
                          </li>
                          <li>
                            If you still cannot solve this error after checking,
                            please contact us{" "}
                            <a href="mailto:work7z@outlook.com">
                              work7z@outlook.com
                            </a>
                          </li>
                        </ul>
                      </div>
                    ) : (
                      ""
                    )}
                    {checkingStatus.done ? (
                      <li style={{ color: "green" }}>Done.</li>
                    ) : (
                      ""
                    )}
                    {/* <li>Local Service Runtime</li>
                    <li>Local Service Core Dependencies</li>
                    <li>Finished this checking</li> */}
                  </ul>
                )}
              </p>
            </div>
            <div className="preli-footer">
              {checkingStatus.done ? (
                <Button
                  intent="success"
                  outlined={true}
                  onClick={() => {
                    location.reload();
                  }}
                >
                  {checkingStatus.doneText}
                </Button>
              ) : (!checkingStatus.start || isAnyError) &&
                !checkingStatus.tryStopLoading ? (
                <Button
                  intent={isAnyError ? "warning" : "primary"}
                  onClick={() => {
                    gutils.api.preli.startInit();
                  }}
                >
                  {isAnyError ? "Try Again" : "Download"}
                </Button>
              ) : (
                <Button
                  loading={checkingStatus.tryStopLoading}
                  outlined={true}
                  intent="danger"
                  onClick={() => {
                    gutils.api.preli.stopInit();
                  }}
                >
                  Cancel
                </Button>
              )}
            </div>
          </div>
        );
      },
      title: "Download",
    },
    example: {
      props: {},
      renderPanel: (props) => {
        return (
          <div className="preli-all-wrap">
            <div className="preli-wrap">example</div>
            <div className="preli-footer">
              <Button
                intent="none"
                onClick={() => {
                  props.openPanel(stackMap.mirrorSetting);
                }}
              >
                Continue
              </Button>
            </div>
          </div>
        );
      },
      title: "Download Settings",
    },
  },
  (x, d, n) => {
    console.log("renderpanel");
    // x.renderPanel = observer(x.renderPanel);
    const RawPanel = x.renderPanel;
    x.renderPanel = (props = {}) => {
      const CrtWork = observer(RawPanel);
      return <CrtWork {...props} />;
    };
    // x.renderPanel = (props = {}) => {
    //   const [update = Math.random(), onUpdate] = useState(null);
    //   return (
    //     <RawPanel
    //       {...props}
    //       onUpdate={() => {
    //         onUpdate(Math.random);
    //       }}
    //       key={update}
    //     />
    //   );
    // };
    return x;
  }
);

class PreliClz extends React.PureComponent {
  state = {
    stackList: [stackMap.prelude],
  };
  removeFromPanelStack = () => {
    this.setState({
      stackList: (this.state.stackList || []).slice(0, -1),
    });
  };
  addToPanelStack = (newPanel) => {
    this.setState({
      stackList: [...this.state.stackList, newPanel],
    });
  };
  render() {
    return (
      <div className="sys-prelimnary-wrapper">
        <Example>
          <PanelStack2
            className="docs-panel-stack-example"
            onOpen={this.addToPanelStack}
            onClose={this.removeFromPanelStack}
            renderActivePanelOnly={true}
            showPanelHeader={true}
            stack={this.state.stackList}
          />
        </Example>
      </div>
    );
  }
}

export default PreliClz;
