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
import gutils from "../utils";
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
import gstore from "../store.js";
import LoadingPage from "../routes/loading/index";
import MainPage from "../routes/main/index";
import "../index.less";
import _ from "lodash";

const myapi = {
  loadEditor: async function () {
    const databaseAllData = gstore.databaseAllData;
    gutils.once("init_auto_load_wlc", () => {
      autorun(() => {
        if (_.isEmpty(databaseAllData.data.editorTab.list)) {
          databaseAllData.data.editorTab.value = "overview";
          databaseAllData.data.editorTab.list = [
            {
              label: "Overview",
              id: "overview",
              type: "overview",
            },
          ];
        }
      });
    });
  },
  loadConnTree: async function () {
    const databaseAllData = gstore.databaseAllData;
    try {
      databaseAllData.data.loadingTree = true;
      const data = databaseAllData.data;
      // const tree = data.connectionList.tree;
      const connListRes = await gutils.opt("/dblink/conn-list");
      const rootRawTree = [connListRes.content];
      function makeFormatting(rootRawTree) {
        const crtLayerArr = [];
        _.forEach(rootRawTree, (x, d, n) => {
          _.forEach(x.EXTRA_DATA_SUB_FOLDER, (xx, dd, nn) => {
            crtLayerArr.push({
              title: xx["FOLDER_NAME"],
              key: "f-" + xx["ID"],
              isConnect: xx["IS_CONNECTION"] == 1,
              meta: {
                ...xx,
                EXTRA_DATA_CONN: [],
                EXTRA_DATA_SUB_FOLDER: [],
              },
              isLeaf: false,
              children: makeFormatting([xx]),
            });
          });
          _.forEach(x.EXTRA_DATA_CONN, (xx, dd, nn) => {
            crtLayerArr.push({
              title: xx["CONNECTION_NAME"],
              key: "c-" + xx["ID"],
              isConnect: xx["IS_CONNECTION"] == 1,
              meta: xx,
              isLeaf: true,
            });
          });
        });
        return crtLayerArr;
      }
      const rootTree = makeFormatting(rootRawTree);
      console.log(rootRawTree, rootTree);
      data.connectionList.tree = rootTree;
      databaseAllData.data.loadingTree = false;
    } catch (err) {
      databaseAllData.data.loadingTree = false;
      throw err;
    }
  },
  menu_moveto: async function (node, to_menu_key) {
    let crtTree = gstore.databaseAllData.data.connectionList.tree;
    let placeNode = null;
    gutils.iterateTree([{ root: 1, children: crtTree }], (e) => {
      let findidx = _.findIndex(e.children, (x) => x.key == node.key);
      if (findidx != -1) {
        e.children = gutils.pickArr(e.children, findidx);
        if (e.root == 1) {
          crtTree = gutils.pickArr(crtTree, findidx);
        }
      }
      if (e.key == to_menu_key) {
        placeNode = e;
      }
    });
    if (!_.isNil(placeNode)) {
      if (_.isNil(placeNode.children)) {
        placeNode.children = [];
      }
      placeNode.children.push(node);
    }
    if (_.isNil(to_menu_key)) {
      crtTree = [...crtTree, node];
    }
    gstore.databaseAllData.data.connectionList.tree = [...crtTree];
    await myapi.saveTreeAndRefreshIt();
  },
  testConn: async function () {
    const addModel = gstore.databaseAllData.addNewConnPageData.addModel;
    console.log("test connection", addModel);
    try {
      gstore.databaseAllData.addNewConnPageData.isLoadingTestConn = true;
      let connTestRes = await gutils.opt(
        "/dblink/conn-test",
        {
          CONN: addModel,
          errorResponse: true,
        },
        {
          ref(source) {
            window.cancelTheTestConn = () => {
              source.cancel();
            };
          },
        }
      );
      gutils.w_alertSuccess(
        "Connected successfully! Elapsed time: " + connTestRes.content + "ms",
        { width: "430px", title: "Test Connection Result" }
      );
      console.log("conn test res");
      gstore.databaseAllData.addNewConnPageData.isLoadingTestConn = false;
    } catch (e) {
      gstore.databaseAllData.addNewConnPageData.isLoadingTestConn = false;
      const themsg = _.get(e, "data.message", _.get(e, "message", e));
      console.log("got error", e);
      gutils.w_alertError(_.toString(themsg), { width: "430px" });
    }
  },
  refresh: async function () {
    await myapi.loadConnTree();
  },
  create_folder(folderNode, crtNodeObj) {
    gstore.databaseAllData.overlay_addNewFolder.open = true;
    const nextObj = _.isNil(crtNodeObj)
      ? gstore.databaseAllData.addNewConnPageData.initModelForFolder
      : crtNodeObj.meta;
    gstore.databaseAllData.addNewConnPageData.addModelForFolder =
      _.cloneDeep(nextObj);
    if (!_.isNil(folderNode)) {
      console.log("add model for folder");
      gstore.databaseAllData.addNewConnPageData.addModelForFolder.PARENT_FOLDER_ID =
        folderNode.meta.ID;
    }
    gutils.clearCache();
  },
  confirm_create_connection: async function () {
    try {
      const model = gstore.databaseAllData.addNewConnPageData.addModel;
      gstore.databaseAllData.overlay_addNewConn.loading = true;
      if (_.isNil(model.CONNECTION_NAME)) {
        model.CONNECTION_NAME = `${model.HOST}:${model.PORT}`;
      }
      const isNewCreate = _.isNil(model.ID);
      if (!isNewCreate) {
        await gutils.opt("/dblink/opt-conn-upset", {
          ...model,
        });
        await myapi.refresh();
      } else {
        const crtTree = gstore.databaseAllData.data.connectionList.tree;
        let newobj = {
          new: true,
          title: model.CONNECTION_NAME,
          meta: _.cloneDeep(model),
          isLeaf: true,
        };
        if (!_.isNil(model.FOLDER_ID)) {
          gutils.iterateTree(
            crtTree,
            (x) => {
              console.log("loop tree", x);
              if (x.meta.ID == model.FOLDER_ID) {
                let gotParent = x;
                if (!_.isNil(gotParent)) {
                  if (_.isNil(gotParent.children)) {
                    gotParent.children = [];
                  }
                  gotParent.children.push(newobj);
                }
              }
            },
            "children"
          );
        } else {
          crtTree.push(newobj);
        }
        gstore.databaseAllData.data.connectionList.tree = [
          ...gstore.databaseAllData.data.connectionList.tree,
        ];
        await myapi.saveTreeAndRefreshIt();
      }
      gstore.databaseAllData.overlay_addNewConn.loading = false;
      gstore.databaseAllData.overlay_addNewConn.open = false;
    } catch (err) {
      gstore.databaseAllData.overlay_addNewConn.loading = false;
      throw err;
    }
  },
  confirm_create_folder: async function () {
    try {
      const addModelForFolder =
        gstore.databaseAllData.addNewConnPageData.addModelForFolder;
      gstore.databaseAllData.overlay_addNewFolder.loading = true;
      const isNewCreate = _.isNil(addModelForFolder.ID);
      if (isNewCreate) {
        await gutils.opt("/dblink/opt-folder-upset", {
          ...addModelForFolder,
        });
        await myapi.refresh();
      } else {
        const crtTree = gstore.databaseAllData.data.connectionList.tree;
        let newobj = {
          new: true,
          title: addModelForFolder.FOLDER_NAME,
          meta: _.cloneDeep(addModelForFolder),
          isLeaf: false,
          children: [],
        };
        if (!_.isNil(addModelForFolder.PARENT_FOLDER_ID)) {
          gutils.iterateTree(
            crtTree,
            (x) => {
              console.log("loop tree", x);
              if (x.meta.ID == addModelForFolder.PARENT_FOLDER_ID) {
                let gotParent = x;
                if (!_.isNil(gotParent)) {
                  if (_.isNil(gotParent.children)) {
                    gotParent.children = [];
                  }
                  gotParent.children.push(newobj);
                }
              }
            },
            "children"
          );
        } else {
          crtTree.push(newobj);
        }
        gstore.databaseAllData.data.connectionList.tree = [
          ...gstore.databaseAllData.data.connectionList.tree,
        ];
        await myapi.saveTreeAndRefreshIt();
      }
      gstore.databaseAllData.overlay_addNewFolder.loading = false;
      gstore.databaseAllData.overlay_addNewFolder.open = false;
    } catch (err) {
      gstore.databaseAllData.overlay_addNewFolder.loading = false;
      throw err;
    }
  },
  saveTreeAndRefreshIt: async function (isRefreshAndAjax = true) {
    try {
      gstore.databaseAllData.overlay_addNewFolder.loading = true;

      let crtTree = gstore.databaseAllData.data.connectionList.tree;
      let callFunc = (crtTree) => {
        if (_.isEmpty(crtTree)) {
          return null;
        }
        let crtObj = {
          root: 0,
          EXTRA_DATA_CONN: [],
          EXTRA_DATA_SUB_FOLDER: [],
        };
        _.forEach(crtTree, (x, d, n) => {
          if (x.isLeaf) {
            if (_.isNil(crtObj.EXTRA_DATA_CONN)) {
              crtObj.EXTRA_DATA_CONN = [];
            }
            crtObj.EXTRA_DATA_CONN.push({
              CONNECTION_NAME: x["title"],
              ID: gutils.noNaNWithNull(
                parseInt(("" + x["key"]).replaceAll("c-", ""))
              ),
              ...(x["meta"] || {}),
              EXTRA_DATA_meta: x["meta"],
            });
          } else {
            let nextChildObj = callFunc(x.children);
            if (_.isNil(nextChildObj)) {
              nextChildObj = {};
            }
            if (_.isNil(crtObj.EXTRA_DATA_SUB_FOLDER)) {
              crtObj.EXTRA_DATA_SUB_FOLDER = [];
            }
            crtObj.EXTRA_DATA_SUB_FOLDER.push({
              FOLDER_NAME: x["title"],
              ID: gutils.noNaNWithNull(
                parseInt(("" + x["key"]).replaceAll("f-", ""))
              ),
              EXTRA_DATA_meta: x["meta"],
              ...(x["meta"] || {}),
              EXTRA_DATA_SUB_FOLDER: nextChildObj.EXTRA_DATA_SUB_FOLDER,
              EXTRA_DATA_CONN: nextChildObj.EXTRA_DATA_CONN,
            });
          }
        });
        return crtObj;
      };
      let finalTree = callFunc(crtTree);
      if (finalTree == null) {
        finalTree = {};
      }
      finalTree.root = 1;
      console.log("save and refresh", finalTree, crtTree);
      let connSaveRes = await gutils.opt("/dblink/conn-save", {
        CONN_DATA: finalTree,
      });
      if (isRefreshAndAjax) {
        await myapi.refresh();
      }
      gstore.databaseAllData.overlay_addNewFolder.loading = false;
    } catch (err) {
      gstore.databaseAllData.overlay_addNewFolder.loading = false;
      throw err;
    }
  },
  create_connection: async function (folderNode, currentConnNode) {
    gstore.databaseAllData.overlay_addNewConn.open = true;

    let newModel = null;
    if (_.isNil(currentConnNode)) {
      newModel = _.cloneDeep(
        gstore.databaseAllData.addNewConnPageData.initModel
      );
    } else {
      newModel = _.cloneDeep(currentConnNode.meta);
    }
    gstore.databaseAllData.addNewConnPageData.addModel = newModel;

    if (!_.isEmpty(folderNode)) {
      gstore.databaseAllData.addNewConnPageData.addModel.FOLDER_ID =
        folderNode.meta.ID;
    }

    gstore.databaseAllData.addNewConnPageData.formNeeds.dbTypes_loading = true;
    // query basic data
    let queryAllData = await gutils.opt("/dblink/dbtype-query-all");
    let finalDatabaseType = [];
    _.forEach(queryAllData.content, (x, d, n) => {
      finalDatabaseType.push({
        label: x["DATABASE_NAME"],
        value: x["ID"],
      });
    });
    const addNewConnPageData = gstore.databaseAllData.addNewConnPageData;
    const addModel = gstore.databaseAllData.addNewConnPageData.addModel;
    gutils.once("init_for_the_prop", () => {
      autorun(() => {
        let crtDatabaseTypeId =
          gstore.databaseAllData.addNewConnPageData.addModel.DBTYPE_ID;
        if (!_.isNil(crtDatabaseTypeId)) {
          myapi.chooseDbTypeAndInitRelated(crtDatabaseTypeId);
        } else {
          addNewConnPageData.formNeeds.relatedDrivers = [];
        }
      });
      autorun(async () => {
        myapi.downloadDriver();
      });
    });
    // assign and auto execute first one
    addNewConnPageData.formNeeds.dbTypes = finalDatabaseType;
    addNewConnPageData.addModel.DBTYPE_ID = _.get(finalDatabaseType, "0.value");
    // finish the basic connection
    gstore.databaseAllData.addNewConnPageData.formNeeds.dbTypes_loading = false;
    gutils.clearCache();
  },
  downloadDriver: async function () {
    let crtDriverId =
      gstore.databaseAllData.addNewConnPageData.addModel.DRIVER_ID;
    if (!_.isNil(crtDriverId)) {
      let res = await gutils.opt("/dblink/download-driver", {
        DRIVER_ID: crtDriverId,
      });
      gstore.databaseAllData.addNewConnPageData.formNeeds.driver_download_uid =
        gutils.uuid();
      // start downloading the driver files
      gutils.defer(async () => {
        const saveFile = null;
        const { groupId, artifactId, version, base, folder } = res.content;
        console.log("got source", res.content);
        const driver_downloadStatus =
          gstore.databaseAllData.addNewConnPageData.formNeeds
            .driver_downloadStatus;
        // do download
        driver_downloadStatus.status = "init";
        let myst = driver_downloadStatus;
        let crtTypeArr = [".jar"];
        let idx = 0;
        for (let crtType of crtTypeArr) {
          idx++;
          await new Promise((resolve, reject) => {
            myst.desc = `${idx}/${_.size(crtTypeArr)}`;
            window.ipc.downloadJar(
              {
                groupId,
                artifactId,
                version,
                type: crtType,
              },
              {
                onProgress(e) {
                  myst.status = "started";
                  myst.currentSize = e.loaded_num;
                  myst.totalSize = e.total_num;
                },
                onSuccess: async function () {
                  // do verify
                  myst.status = "done";
                  resolve();
                },
                onFail(err) {
                  myst.status = "error";
                  myst.errMsg = err;
                  reject();
                },
              }
            );
          });
        }
        await gutils.sleep(500);
        gstore.databaseAllData.addNewConnPageData.formNeeds.driver_download_uid =
          null;
        // do verify
      });
    }
  },
  chooseDbTypeAndInitRelated: async function (crtDatabaseTypeId) {
    gstore.databaseAllData.addNewConnPageData.formNeeds.relatedDrivers_loading = true;
    let queryDrivers = await gutils.opt("/dblink/driver-custom-list", {
      DBTYPE_ID: crtDatabaseTypeId,
    });
    gstore.databaseAllData.addNewConnPageData.formNeeds.relatedDrivers = _.map(
      queryDrivers.content,
      (x, d, n) => {
        return {
          label: x["DRIVER_NAME"],
          value: x["ID"],
          meta: x,
        };
      }
    );
    let relatedDrivers =
      gstore.databaseAllData.addNewConnPageData.formNeeds.relatedDrivers;
    gstore.databaseAllData.addNewConnPageData.addModel.DRIVER_ID = _.chain(
      relatedDrivers
    )
      .find(
        (x) => x.label.indexOf("-rc") == -1 && x.label.indexOf("-dmr") == -1
      )
      .thru((x) => {
        if (_.isNil(x)) {
          return _.get(relatedDrivers, "0");
        } else {
          return x;
        }
      })
      .get("value")
      .value();
    gstore.databaseAllData.addNewConnPageData.formNeeds.relatedDrivers_loading = false;
  },
  openAllConnections() {},
  closeAllConnections() {},
  formatFolderAndCreateNew(node) {
    return _.chain(node)
      .cloneDeep()
      .merge({
        title: node.title + "-1",
        key: node.key + "-1",
        meta: {
          FOLDER_NAME: node.meta.FOLDER_NAME + "-1",
          ID: null,
        },
        children: gutils.iterateTree(node.children, (x, d, n) => {
          if (x.isLeaf) {
            return _.merge(myapi.formatNodeAndCreateNew(x), {
              label: x.label,
              meta: {
                ID: null,
                CONNECTION_NAME: x.meta.CONNECTION_NAME,
              },
            });
          } else {
            return _.merge(myapi.formatFolderAndCreateNew(x), {
              label: x.label,
              meta: {
                ID: null,
                FOLDER_NAME: x.meta.FOLDER_NAME,
              },
            });
          }
        }),
      })
      .value();
  },
  duplicate_folder: async function (node) {
    console.log("duplicate connection", node, node.meta);
    const crtTree = gstore.databaseAllData.data.connectionList.tree;
    const nextJob = myapi.formatFolderAndCreateNew(node);
    gutils.iterateTree(
      [nextJob],
      (x) => {
        _.set(x, "meta.ID", null);
      },
      "children"
    );
    console.log("duplicate order nextjob", nextJob);
    let isFind = false;
    gutils.iterateTree(
      crtTree,
      (item) => {
        let findItem = _.find(item.children, (x) => x.key == node.key);
        if (!_.isNil(findItem)) {
          item.children.push(nextJob);
          isFind = true;
        }
      },
      "children"
    );
    if (!isFind) {
      gstore.databaseAllData.data.connectionList.tree = [
        ...gstore.databaseAllData.data.connectionList.tree,
        nextJob,
      ];
    }
    gstore.databaseAllData.data.connectionList.tree = [
      ...gstore.databaseAllData.data.connectionList.tree,
    ];
    await myapi.saveTreeAndRefreshIt();
  },
  delete_folder: async function (node) {
    let folderID = node.meta.ID;
    console.log("deleing the folder by id", folderID);
    if (
      !gutils.confirm(
        "Do you want to delete this folder and related sub connections?"
      )
    ) {
      return;
    }
    await gutils.opt("/dblink/opt-folder-delete", {
      ID: folderID,
    });
    await myapi.refresh();
  },
  delete_connection: async function (node) {
    let connID = node.meta.ID;
    console.log("deleing the conn by id", connID);
    if (!gutils.confirm("Do you want to delete this connection?")) {
      return;
    }
    await gutils.opt("/dblink/opt-conn-delete", {
      ID: connID,
    });
    await myapi.refresh();
  },
  formatNodeAndCreateNew(node) {
    return _.chain(node)
      .cloneDeep()
      .merge({
        title: node.title + "-1",
        key: node.key + "-1",
        meta: {
          CONNECTION_NAME: node.meta.CONNECTION_NAME + "-1",
          ID: null,
        },
      })
      .value();
  },
  duplicate_connection: async function (node) {
    console.log("duplicate connection", node, node.meta);
    const crtTree = gstore.databaseAllData.data.connectionList.tree;
    const nextJob = myapi.formatNodeAndCreateNew(node);
    let isFind = false;
    gutils.iterateTree(
      crtTree,
      (item) => {
        let findItem = _.find(item.children, (x) => x.key == node.key);
        if (!_.isNil(findItem)) {
          item.children.push(nextJob);
          isFind = true;
        }
      },
      "children"
    );
    if (!isFind) {
      gstore.databaseAllData.data.connectionList.tree = [
        ...gstore.databaseAllData.data.connectionList.tree,
        nextJob,
      ];
    }
    gstore.databaseAllData.data.connectionList.tree = [
      ...gstore.databaseAllData.data.connectionList.tree,
    ];
    await myapi.saveTreeAndRefreshIt();
  },
  edit_connection() {},
  rename_folder() {},
  rename_connection() {},
};

export default myapi;
