import {
  PayloadAction,
  Store,
  configureStore,
  createAsyncThunk,
  getDefaultMiddleware,
} from "@reduxjs/toolkit";
import { listenerMiddleware } from "./listenerMiddleware";
import rootReducer from "./slice";
import thunk from "redux-thunk";
import apiSlice from "./slice/apiSlice";
import nocycle from "./nocycle";
import _ from "lodash";
import { logutils } from "./utils/LogUtils";

const alwaysHappyMiddleware =
  (storeAPI) => (next) => (action: PayloadAction) => {
    const originalResult = next(action);
    // check forge
    if (_.startsWith(action.type, "forge/")) {
      // let state = storeAPI.getState();
      // let forge = state.forge;
      // logutils.debug("saving forge", state, action);
      // saveIntoForge2(forge);
      // logutils.debug("saved forge");
    }
    return originalResult;
  };

export default function configureAppStore() {
  const store = configureStore({
    reducer: rootReducer,
    middleware: (getDefaultMiddleware) => {
      return getDefaultMiddleware()
        .concat(apiSlice.middleware)
        .concat(alwaysHappyMiddleware)
        .prepend(listenerMiddleware.middleware);
    },
    // preloadedState, // TODO: restore previous session
    enhancers: [],
  });

  // TODO: hotfix
  //   if (process.env.NODE_ENV !== "production" && (module as any).hot) {
  //     (module as any).hot.accept("./reducers", () =>
  //       store.replaceReducer(rootReducer)
  //     );
  //   }

  return store;
}
