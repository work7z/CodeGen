import React, { useEffect } from "react";
import "./App.css";
import { useSelector, useDispatch } from "react-redux";

import { store, RootState } from "./store/index";
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

function App() {
  return <div>hello, world</div>;
  // <HotkeysProvider>
  //   <Router basename={URLUtils.GetRoutePath("")}>
  //     <div style={{ width: "100%", height: "100%" }}>{innerJSX}</div>
  //   </Router>
  // </HotkeysProvider>
}

export default App;
