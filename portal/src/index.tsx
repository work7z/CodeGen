import React from "react";
import ReactDOM from "react-dom/client";
import { Provider } from "react-redux";
import App from "./App";
import reportWebVitals from "./reportWebVitals";
import { store, RootState } from "./store/index";
import "purecss/build/pure.css";
import "@blueprintjs/core/lib/css/blueprint.css";
import "@blueprintjs/icons/lib/css/blueprint-icons.css";
import "./system.scss";

import ALL_NOCYCLE from "./nocycle";

ALL_NOCYCLE.store = store;

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

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
