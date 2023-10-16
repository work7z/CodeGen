import _ from "lodash";
import gutils from "./GlobalUtils";
import staticDevJson from "../static/dev.json";
import URLUtils from "./URLUtils";
import { Dot } from "./TranslationUtils";

let hookStatusMap: { [key: string]: () => void | null } = {};

const ConcurrencyUtils = {
  initFunc: (key: string, fn: () => void) => {
    let st = hookStatusMap[key];
    if (_.isNil(st)) {
      let cachedFn = _.once(fn);
      hookStatusMap[key] = cachedFn;
      cachedFn();
    }
  },
  removeInitStatus: (key: string) => {
    delete hookStatusMap[key];
  },
};

export default ConcurrencyUtils;
