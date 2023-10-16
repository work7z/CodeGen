import React, { useEffect } from "react";
import "./App.css";
import { useSelector, useDispatch } from "react-redux";

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
} from "react-router-dom";
import URLUtils from "./utils/URLUtils";
import AuthHookUtils from "./utils/AuthHookUtils";

function App() {
  let innerJSX = <div>hello, world</div>;
  return (
    <HotkeysProvider>
      <Router basename={URLUtils.GetRoutePath("")}>
        <div style={{ width: "100%", height: "100%" }}>{innerJSX}</div>
      </Router>
    </HotkeysProvider>
  );
}

export default App;
