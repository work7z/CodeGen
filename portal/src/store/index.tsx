import configureStore from "../configureStore";
import _ from "lodash";
const store = configureStore();
export type RootState = ReturnType<typeof store.getState>;
export { store };
_.set(window, "gstore", store);
