import { ToolkitStore } from "@reduxjs/toolkit/dist/configureStore";
import { RootState } from "./store";
import appinfoJSON from "./app-info.json";

interface NoCycle {
  store?: ToolkitStore<RootState>;
}
let ALL_NOCYCLE: NoCycle = {};
export type RootState2 = RootState;
export const APPINFOJSON = appinfoJSON;

export const FN_GetDispatch = () => {
  return ALL_NOCYCLE.store?.dispatch;
};
export const FN_GetState = (): RootState2 => {
  return ALL_NOCYCLE.store?.getState() as RootState2;
};

export default ALL_NOCYCLE;
