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
  ContextMenu,
  NumericInput,
  FormGroup,
  HTMLSelect,
  ControlGroup,
  InputGroup,
  Navbar,
  NavbarHeading,
  MenuDivider,
  NonIdealState,
  NavbarDivider,
  ContextMenuTarget,
  NavbarGroup,
  Menu,
  MenuItem,
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
import GTree from "../../components/GTree/index.js";

export default observer(() => {
  const showContextMenuForBody = (e) => {
    // must prevent default to cancel parent's context menu
    e.preventDefault();
    // invoke static API, getting coordinates from mouse event
    // eslint-disable-next-line deprecation/deprecation
    if (window.ackByTree) {
      window.ackByTree = false;
      return;
    }
    if (true) {
      // return;
    }
    ContextMenu.show(
      <Menu>
        <MenuItem
          onClick={() => {
            gutils.api.dblink.create_folder();
          }}
          icon="folder-new"
          text="Create Folder"
        />
        <MenuItem
          onClick={() => {
            gutils.api.dblink.create_connection();
          }}
          icon="add-to-artifact"
          text="Create Connection"
        />
        <MenuDivider />
        <MenuItem
          onClick={() => {
            gutils.api.dblink.openAllConnections();
          }}
          icon="data-connection"
          text="Open All Connections"
        />
        <MenuItem
          onClick={() => {
            gutils.api.dblink.closeAllConnections();
          }}
          icon="remove-column-left"
          text="Close All Connections"
        />
        <MenuDivider />
        <MenuItem
          onClick={() => {
            gutils.api.dblink.refresh();
          }}
          icon="refresh"
          text="Refresh"
        />
      </Menu>,
      { left: e.clientX, top: e.clientY },
      () => {}
    );
  };

  return (
    <div style={{ height: "100%" }}>
      <div className="db_conn_project_title">Connections</div>
      <div
        className="db_conn_project_body"
        onContextMenu={showContextMenuForBody}
      >
        <div>
          <GTree
            chgfunc={() => {
              gutils.defer(() => {
                // gutils.api.dblink.saveTreeAndRefreshIt(false);
              });
            }}
            unikey="db_conn_tree"
            onRightClick={({ event, node }) => {
              const e = event;
              console.log("right click menu", event, node);
              window.ackByTree = true;
              // open context menu
              // must prevent default to cancel parent's context menu
              e.preventDefault();
              // invoke static API, getting coordinates from mouse event
              // eslint-disable-next-line deprecation/deprecation
              const isFolder = node.isLeaf == false;
              const crtTree = gstore.databaseAllData.data.connectionList.tree;

              function nextChildren(arr) {
                return _.map(
                  _.filter(arr, (x) => x.isLeaf != true),
                  (x, d, n) => {
                    let myval = nextChildren(x.children || []);
                    if (_.isEmpty(myval)) {
                      myval = [];
                    }
                    myval = [
                      <MenuItem
                        onClick={() => {
                          console.log("move the item to root");
                          gutils.api.dblink.menu_moveto(node, x.key);
                        }}
                        icon="folder-close"
                        text={"<Folder Root>"}
                      ></MenuItem>,
                      ...myval,
                    ];
                    let commonProps = {
                      key: x.key,
                      onClick() {
                        console.log("move the item to", x.key);
                        gutils.api.dblink.menu_moveto(node, x.key);
                      },
                      icon: "folder-close",
                      text: x.title,
                    };
                    return _.isEmpty(myval) ? (
                      <MenuItem {...commonProps}></MenuItem>
                    ) : (
                      <MenuItem {...commonProps}>{myval}</MenuItem>
                    );
                  }
                );
              }
              let crtChildren = nextChildren(crtTree);
              crtChildren = [
                <MenuItem
                  onClick={() => {
                    console.log("move the item to root");
                    gutils.api.dblink.menu_moveto(node, null);
                  }}
                  icon="folder-close"
                  text={"<Root>"}
                ></MenuItem>,
                ...crtChildren,
              ];
              let menu_moveto = (
                <MenuItem icon="share" text="Move to">
                  {crtChildren}
                </MenuItem>
              );
              if (isFolder) {
                ContextMenu.show(
                  <Menu>
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.create_folder(node);
                      }}
                      icon="folder-new"
                      text="Create Folder"
                    />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.create_connection(node);
                      }}
                      icon="add-to-artifact"
                      text="Create Connection"
                    />
                    <MenuItem icon="annotation" text="Edit">
                      {/* <MenuItem
                        onClick={() => {
                          gutils.api.dblink.rename_folder(node);
                        }}
                        icon="array"
                        text="Name"
                      /> */}
                      <MenuItem
                        onClick={() => {
                          gutils.api.dblink.create_folder(null, node);
                        }}
                        icon="cog"
                        text="Folder Info"
                      />
                    </MenuItem>
                    {menu_moveto}
                    <MenuDivider />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.duplicate_folder(node);
                      }}
                      icon="duplicate"
                      text="Duplicate Folder"
                    />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.delete_folder(node);
                      }}
                      intent="danger"
                      icon="remove-column-left"
                      text="Delete Folder"
                    />
                    <MenuDivider />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.refresh();
                      }}
                      icon="refresh"
                      text="Refresh"
                    />
                  </Menu>,
                  { left: e.clientX, top: e.clientY },
                  () => {}
                );
              } else {
                ContextMenu.show(
                  <Menu>
                    <MenuItem icon="panel-table" text="SQL Editor">
                      <MenuItem
                        onClick={() => {
                          gutils.api.dblink.script_new(node);
                        }}
                        icon="document"
                        text="New Script"
                      />
                      <MenuItem
                        onClick={() => {
                          gutils.api.dblink.script_view_recent(node);
                        }}
                        icon="history"
                        text="Recent Scripts"
                      />
                    </MenuItem>
                    <MenuItem icon="annotation" text="Edit">
                      {/* <MenuItem
                        onClick={() => {
                          gutils.api.dblink.edit_name(node);
                        }}
                        icon="array"
                        text="Name"
                      /> */}
                      <MenuItem
                        onClick={() => {
                          gutils.api.dblink.create_connection(null, node);
                        }}
                        icon="cog"
                        text="Config"
                      />
                    </MenuItem>
                    {menu_moveto}
                    <MenuDivider />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.duplicate_connection(node);
                      }}
                      icon="duplicate"
                      text="Duplicate Connection"
                    />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.delete_connection(node);
                      }}
                      intent="danger"
                      icon="remove-column-left"
                      text="Delete Connection"
                    />
                    <MenuDivider />
                    <MenuItem
                      onClick={() => {
                        gutils.api.dblink.refresh();
                      }}
                      icon="refresh"
                      text="Refresh"
                    />
                  </Menu>,
                  { left: e.clientX, top: e.clientY },
                  () => {}
                );
              }
            }}
            obj={gstore.databaseAllData.data.connectionList}
            index="tree"
          />
        </div>
      </div>
      <div className="db_conn_project_foot doflex">
        <div>
          <Button
            onClick={() => {
              gutils.api.dblink.create_folder();
            }}
            icon="folder-new"
            minimal={true}
          ></Button>
          <Button
            onClick={() => {
              gutils.api.dblink.create_connection();
            }}
            icon="add-to-artifact"
            minimal={true}
          ></Button>
        </div>
        <div>
          <Button
            onClick={() => {
              gutils.api.dblink.refresh();
            }}
            icon="refresh"
            loading={gstore.databaseAllData.data.loadingTree}
            minimal={true}
          ></Button>
        </div>
      </div>
    </div>
  );
});
