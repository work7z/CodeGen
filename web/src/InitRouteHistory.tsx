import React, { useEffect } from "react";
import "./App.css";
import { useSelector, useDispatch } from "react-redux";
import testReducer, { pong, testSliceActions } from "./slice/testSlice";
import { store, RootState } from "./store/index";
import exportUtils from "./utils/ExportUtils";
import { logutils } from "./utils/LogUtils";
import { HotkeysProvider, HotkeysTarget2 } from "@blueprintjs/core";
import {
  withRouter,
  BrowserRouter as Router,
  Switch,
  Route,
  Link,
  useHistory,
  Redirect,
  useParams,
  useRouteMatch,
} from "react-router-dom";
import Welcome from "./pages/Welcome";
import WorkBench from "./pages/WorkBench";
import Setup from "./pages/Setup";
import $ from "jquery";
import _ from "lodash";
import { CLZ_ROOT_DARK, CLZ_ROOT_LIGHT } from "./styles/var";
import InitSystemEnv from "./pages/InitSystemEnv";
import UserAskMultipleDialogs from "./biz/UserAskMultipleDialogs";
import gutils from "./utils/GlobalUtils";
import TranslationUtils from "./utils/TranslationUtils";
import URLUtils from "./utils/URLUtils";
import {
  ID_FILES,
  ID_HISTORY,
  ID_NOTES,
  ID_TOOLS,
  URL_WORKBENCH,
  URL_WORKBENCH_FILES,
  URL_WORKBENCH_HISTORY,
  URL_WORKBENCH_NOTES,
  URL_WORKBENCH_TOOLS,
} from "./styles/path";
import RouteMem from "./styles/routeMem";

let InitRouteHistory = _.once(() => {
  const hist = useHistory();
  const dispatch = exportUtils.dispatch();
  hist.listen((val) => {
    logutils.log("route changed", val);
    let mapList: { pathname: string; id: string }[] = [
      {
        pathname: URL_WORKBENCH_TOOLS,
        id: ID_TOOLS,
      },
      {
        pathname: URL_WORKBENCH_FILES,
        id: ID_FILES,
      },
      {
        pathname: URL_WORKBENCH_HISTORY,
        id: ID_HISTORY,
      },
      {
        pathname: URL_WORKBENCH_NOTES,
        id: ID_NOTES,
      },
    ];
    _.forEach(mapList, (x) => {
      if (val.pathname.startsWith(x.pathname)) {
        RouteMem[x.id] = val.pathname;
      }
    });
  });
});

export default InitRouteHistory;
