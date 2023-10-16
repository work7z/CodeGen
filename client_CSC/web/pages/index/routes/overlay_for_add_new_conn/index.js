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
  Tabs,
  Tab,
  Position,
  Toaster,
  Checkbox,
  NumericInput,
  FormGroup,
  HTMLSelect,
  Menu,
  MenuItem,
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
import { Select } from "@blueprintjs/select";
import gutils from "../../utils";
import { useState, useEffect } from "react";
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
import GTabCentreView from "../../components/GTabCentreView/index";
import GFormBoundView from "../../components/GFormBoundView/index";
import GSyncSelectWithFilter from "../../components/GSyncSelectWithFilter";
import DownloadStatus from "../../components/DownloadStatus";
import GFormInput from "../../components/GFormInput";
import GFormCheckbox from "../../components/GFormCheckbox";
import _ from "lodash";
import CallOutAndView from "../../components/CallOutAndView";

export default () => {
  const [disableRight, onDisableRight] = useState(true);
  useEffect(() => {
    const stopRef = autorun(() => {
      const addModel = gstore.databaseAllData.addNewConnPageData.addModel;
      const confirmDisable =
        gutils.anyEmpty([
          addModel.HOST,
          addModel.PORT,
          addModel.DEFAULT_DATABASE,
          addModel.USERNAME,
          addModel.CONNECTION_NAME,
        ]) ||
        gutils.anyMax([
          {
            value: addModel.CONNECTION_NAME,
            max: 90,
          },
          {
            value: addModel.CONNECTION_BRIEF,
            max: 450,
          },
          {
            value: addModel.HOST,
            max: 100,
          },
          {
            value: addModel.PASSWORD,
            max: 200,
          },
          {
            value: addModel.PORT,
            max: 100,
          },
          {
            value: addModel.USERNAME,
            max: 100,
          },
          {
            value: addModel.DEFAULT_DATABASE,
            max: 100,
          },
        ]);
      // password is not mandatory
      if (
        (gutils.empty(addModel.CONNECTION_NAME) ||
          addModel.EXTRA_DATA_IS_SYS_IPT_NAME == 1) &&
        (!gutils.empty(addModel.HOST) || !gutils.empty(addModel.PORT))
      ) {
        addModel.CONNECTION_NAME = `${
          !_.isNil(addModel.HOST) ? addModel.HOST : ""
        }:${!_.isNil(addModel.PORT) ? addModel.PORT : ""}`;
        addModel.EXTRA_DATA_IS_SYS_IPT_NAME = 1;
      }
      console.log("checking disable", { confirmDisable, disableRight });
      gutils.whenBlurFunc.push(() => {
        onDisableRight(confirmDisable);
      });
    });
    return () => {
      stopRef();
    };
  }, []);
  console.log("rendering confirm disabled", disableRight);
  return (
    <div>
      <DialogCommon
        clzname="tiny-view addnewconn-box"
        confirmDisable={disableRight}
        confirm={() => {
          gutils.api.dblink.confirm_create_connection();
        }}
        style={{}}
        resize={true}
        noBackdrop={true}
        obj={gstore.databaseAllData.overlay_addNewConn}
        left2Content={
          <Button
            disabled={disableRight}
            text="Test Connection"
            onClick={() => {
              gutils.api.dblink.testConn();
            }}
            style={{ left: 0, position: "absolute" }}
          ></Button>
        }
        jsx={(props) => (
          <div className="gform-addconn tiny-form-box">
            <GTabCentreView
              viewObj={gstore.databaseAllData.addNewConnPageData}
              viewKey={"viewkey"}
              tab={[
                {
                  label: "Main",
                  id: "main",
                  jsx: () => {
                    return (
                      <div>
                        <GFormBoundView
                          label="Database"
                          jsx={() => {
                            return (
                              <div>
                                <FormGroup label="Database Type:" inline={true}>
                                  <GSyncSelectWithFilter
                                    obj={
                                      gstore.databaseAllData.addNewConnPageData
                                        .addModel
                                    }
                                    list={
                                      gstore.databaseAllData.addNewConnPageData
                                        .formNeeds.dbTypes
                                    }
                                    loading={
                                      gstore.databaseAllData.addNewConnPageData
                                        .formNeeds.dbTypes_loading
                                    }
                                    icon="database"
                                    index={"DBTYPE_ID"}
                                  />
                                </FormGroup>
                                <FormGroup
                                  label="Database Driver:"
                                  inline={true}
                                >
                                  <div>
                                    <GSyncSelectWithFilter
                                      list={
                                        gstore.databaseAllData
                                          .addNewConnPageData.formNeeds
                                          .relatedDrivers
                                      }
                                      loading={
                                        gstore.databaseAllData
                                          .addNewConnPageData.formNeeds
                                          .relatedDrivers_loading
                                      }
                                      icon="cargo-ship"
                                      obj={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel
                                      }
                                      index={"DRIVER_ID"}
                                    />
                                    {/* <Button
                                      outlined={true}
                                      style={{ marginLeft: "3px" }}
                                      icon="add"
                                    />
                                    <Button
                                      outlined={true}
                                      style={{ marginLeft: "3px" }}
                                      icon="cog"
                                    /> */}
                                  </div>
                                </FormGroup>
                              </div>
                            );
                          }}
                        />
                        <DownloadStatus
                          obj={
                            gstore.databaseAllData.addNewConnPageData.formNeeds
                              .driver_downloadStatus
                          }
                          label="Driver"
                          desc="Initializing files for the connection"
                          uid={
                            gstore.databaseAllData.addNewConnPageData.formNeeds
                              .driver_download_uid
                          }
                          retry={() => {
                            gstore.databaseAllData.addNewConnPageData.formNeeds.driver_download_uid =
                              null;
                            gutils.defer(() => {
                              gutils.api.dblink.downloadDriver();
                            }, 300);
                          }}
                        />
                        {gstore.databaseAllData.addNewConnPageData
                          .isLoadingTestConn ? (
                          <CallOutAndView
                            title="Testing the Connection"
                            desc="This action is being processed"
                            extraJSXLabel="Cancel"
                            extraJSXFunc={() => {
                              gstore.databaseAllData.addNewConnPageData.isLoadingTestConn = false;
                              if (window.cancelTheTestConn) {
                                window.cancelTheTestConn();
                              }
                            }}
                            intent="primary"
                          />
                        ) : (
                          ""
                        )}
                        <GFormBoundView
                          label="Connection"
                          jsx={() => {
                            return (
                              <div>
                                <div className="full-one-box ">
                                  <FormGroup label="Name:" inline={true}>
                                    <GFormInput
                                      small={true}
                                      placeholder="e.g. test connection"
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel
                                          .CONNECTION_NAME
                                      }
                                      onChangeDelay={(val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.CONNECTION_NAME =
                                          val;
                                        gstore.databaseAllData.addNewConnPageData.addModel.EXTRA_DATA_IS_SYS_IPT_NAME = 2;
                                      }}
                                    />
                                  </FormGroup>
                                </div>
                                <div className="full-one-box">
                                  <FormGroup label="Description:" inline={true}>
                                    <GFormInput
                                      type="text"
                                      small={true}
                                      placeholder="e.g. it's used as a test connection"
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel
                                          .CONNECTION_BRIEF
                                      }
                                      onChangeDelay={(val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.CONNECTION_BRIEF =
                                          val;
                                      }}
                                    />
                                  </FormGroup>
                                </div>
                              </div>
                            );
                          }}
                        />

                        <GFormBoundView
                          label="Server"
                          jsx={() => {
                            return (
                              <div>
                                <div className="server-config-box">
                                  <FormGroup label="Host:" inline={true}>
                                    <GFormInput
                                      small={true}
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel.HOST
                                      }
                                      onChangeDelay={gutils.delay((val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.HOST =
                                          val;
                                      })}
                                      placeholder={"e.g. 192.168.2.10"}
                                    />
                                  </FormGroup>
                                  <FormGroup label="Port:" inline={true}>
                                    <GFormInput
                                      type={"number"}
                                      small={true}
                                      placeholder="e.g. 3306"
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel.PORT
                                      }
                                      onChangeDelay={(val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.PORT =
                                          val;
                                      }}
                                    />
                                  </FormGroup>
                                </div>
                                <div className="full-one-box">
                                  <FormGroup label="Database:" inline={true}>
                                    <GFormInput
                                      small={true}
                                      placeholder="e.g. testdb"
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel
                                          .DEFAULT_DATABASE
                                      }
                                      onChangeDelay={(val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.DEFAULT_DATABASE =
                                          val;
                                      }}
                                    />
                                  </FormGroup>
                                </div>
                              </div>
                            );
                          }}
                        />
                        <GFormBoundView
                          label="Authentication"
                          jsx={() => {
                            return (
                              <div>
                                <div className="full-one-box">
                                  <FormGroup label="Username:" inline={true}>
                                    <GFormInput
                                      small={true}
                                      placeholder="e.g. testuser"
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel.USERNAME
                                      }
                                      onChangeDelay={(val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.USERNAME =
                                          val;
                                      }}
                                    />
                                  </FormGroup>
                                </div>
                                <div className="server-config-check-box">
                                  <FormGroup label="Password:" inline={true}>
                                    <GFormInput
                                      small={true}
                                      type={"password"}
                                      placeholder={"value can be empty"}
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel.PASSWORD
                                      }
                                      onChangeDelay={(val) => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.PASSWORD =
                                          val;
                                      }}
                                    />
                                  </FormGroup>
                                  <div>
                                    <GFormCheckbox
                                      list={[
                                        {
                                          label: "Save Password Locally",
                                          value: "SAVE",
                                        },
                                      ]}
                                      value={
                                        gstore.databaseAllData
                                          .addNewConnPageData.addModel
                                          .SAVE_PASSWORD_LOCALLY == 1
                                          ? ["SAVE"]
                                          : []
                                      }
                                      onChange={() => {
                                        gstore.databaseAllData.addNewConnPageData.addModel.SAVE_PASSWORD_LOCALLY =
                                          gstore.databaseAllData
                                            .addNewConnPageData.addModel
                                            .SAVE_PASSWORD_LOCALLY == 1
                                            ? 0
                                            : 1;
                                      }}
                                    />
                                  </div>
                                </div>
                              </div>
                            );
                          }}
                        />
                      </div>
                    );
                  },
                },
                {
                  label: "Editor",
                  id: "editor",
                  jsx: () => {
                    return <div>this is editor</div>;
                  },
                },
                {
                  label: "Other",
                  id: "other",
                  jsx() {
                    return <div>this is other</div>;
                  },
                },
              ]}
            />
          </div>
        )}
      />
    </div>
  );
};
