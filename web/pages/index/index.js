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
import gutils from "./utils";
import _ from "lodash";
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
import gstore from "./store.js";
import LoadingPage from "./routes/loading/index";
import MainPage from "./routes/main/index";
import PreInitView from "./routes/prelimnary/index";
import Overlay_playground from "./routes/overlay_playground";
import "./index.less";
import Handling from "./routes/handling/index";
import LoadService from "./routes/loadService/index";
import autoRunFunc from "./auto_run_func.js";

const initInfo = {
  hadReorientWhenInit: false,
};
const SubRoot = () => {
  const hist = useHistory();
  if (!initInfo.hasReorientWhenInit) {
    initInfo.hasReorientWhenInit = true;
    gutils.defer(() => {
      hist.push("/");
    });
  }
  gutils.once("run auto run", () => {
    autoRunFunc(gstore);
  });

  return (
    <div>
      <Switch>
        <Route exact index path="/" component={LoadingPage}></Route>
        <Route exact index path="/prelimnary" component={PreInitView}></Route>
        <Route exact index path="/handling" component={Handling}></Route>
        <Route exact index path="/loadService" component={LoadService}></Route>
        <Route path="/board" component={MainPage}></Route>
      </Switch>
    </div>
  );
};

const Root = () => {
  console.log("gstore", gstore);
  return (
    <Provider store={gstore}>
      <Router>
        <SubRoot></SubRoot>
      </Router>
    </Provider>
  );
};

const { ipc } = window;

async function startFunc() {
  async function preRunFunc() {
    let preInit = _.keys(gutils.api.system.preInit);
    for (let workFuncKey of preInit) {
      const workFunc = gutils.api.system.preInit[workFuncKey];
      try {
        await workFunc();
      } catch (err) {
        console.log(err);
        // TODO: prevent error logging
      }
    }
  }
  const isBrandNew = ipc.isBrandNewAndNeedDownloadInfra();
  if (isBrandNew || ipc.isLocalServerNotStartedUp()) {
    gutils.waitInitializeRefFunc.push(async function () {
      await preRunFunc();
    });
  } else {
    await preRunFunc();
  }
  ReactDOM.render(<Root />, document.querySelector("#root"));
}
startFunc();

// pre-loading scripts
gutils.getScript("app.bundle.js");
