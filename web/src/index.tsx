import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import App from "./App";
import reportWebVitals from "./reportWebVitals";
import { store, RootState } from "./store/index";
import gutils from "./utils/GlobalUtils";
import "purecss/build/pure.css";
import "@blueprintjs/core/lib/css/blueprint.css";
import "@blueprintjs/icons/lib/css/blueprint-icons.css";
import "./system.scss";

import { logutils } from "./utils/LogUtils";
import URLUtils from "./utils/URLUtils";
import InitUtils from "./utils/InitUtils";
import ALL_NOCYCLE from "./nocycle";
import exportUtils from "./utils/ExportUtils";

ALL_NOCYCLE.store = store;

logutils.debug("Lanuch the page...");

const WrapApp = () => {
  const constructedKey = "";
  return <App key={constructedKey} />;
};

export const FinalRootApp = () => {
  return (
    <React.StrictMode>
      <Provider store={store}>
        <WrapApp />
      </Provider>
    </React.StrictMode>
  );
};

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
root.render(<FinalRootApp />);

logutils.debug("rendered.");

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
