import configureStore from "../configureStore";
import _ from "lodash";
import gutils from "../utils/GlobalUtils";
const store = configureStore();
export type RootState = ReturnType<typeof store.getState>;
export { store };
_.set(window, "gstore", store);
