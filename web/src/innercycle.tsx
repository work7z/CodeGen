import { ToolkitStore } from "@reduxjs/toolkit/dist/configureStore";
import { RootState } from "./store";
import { LANG_EN_US } from "./styles/var";

interface InnerCycle {
  CachedLanguage: string | null;
}
const INNER_CYCLE: InnerCycle = {
  CachedLanguage: null,
};

export default INNER_CYCLE;
