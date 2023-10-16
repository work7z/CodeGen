import _ from "lodash";
import gutils from "./GlobalUtils";
import staticDevJson from "../static/dev.json";
import URLUtils from "./URLUtils";

const InitUtils = {
  InitAllWithDOM: async () => {
    await URLUtils._Init();
  },
};
export default InitUtils;
