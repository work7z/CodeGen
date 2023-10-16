// SKIP_DOT
import _ from "lodash";
import ALL_NOCYCLE from "../nocycle";
import { LANG_EN_US, LangDefinition } from "../styles/var";
import gutils from "./GlobalUtils";
import { logutils } from "./LogUtils";
import { VER_FORGE_FORM } from "../styles/config";

export const KEY_LANG_PACK_ZH_CN = "KEY_LANG_PACK_ZH_CN" + VER_FORGE_FORM;
export const KEY_LANG_PACK_ZH_HK = "KEY_LANG_PACK_ZH_HK" + VER_FORGE_FORM;

interface LangMap {
  zh_CN: LangDefinition;
  zh_HK: LangDefinition;
}
let newLangMap2 = (): LangMap => {
  return {
    zh_CN: {},
    zh_HK: {},
  };
};
export const newLangMap = newLangMap2;
let crtNewLangMap = newLangMap();

export const LANG_INIT_BEFORE_MAP: { [key: string]: boolean } = {};

try {
  let m1 = gutils.safeparse(localStorage.getItem(KEY_LANG_PACK_ZH_CN));
  if (!_.isNil(m1) && _.isObject(m1)) {
    crtNewLangMap.zh_CN = m1 as LangDefinition;
  }
  // do same for KEY_LANG_PACK_ZH_HK
  let m2 = gutils.safeparse(localStorage.getItem(KEY_LANG_PACK_ZH_HK));
  if (!_.isNil(m2) && _.isObject(m2)) {
    crtNewLangMap.zh_HK = m2 as LangDefinition;
  }
} catch (e) {
  // do nothing
}

function formatResultWithReplacer(val = "", ...args) {
  if (_.isNil(args)) {
    args = [];
  }
  for (let index in args) {
    let tval = args[index];
    val = val.replaceAll("{" + index + "}", tval);
  }
  return val;
}

const TranslationUtils = {
  CurrentLanguage: LANG_EN_US,
  LangMap: crtNewLangMap,
  RealtimeObj: {},
  Dot(id: string, enText: string, ...args: any[]): string {
    let language = TranslationUtils.CurrentLanguage;
    if (language == LANG_EN_US) {
      // do nothing
    } else {
      let langmap = TranslationUtils.LangMap;
      let o = langmap[language] as LangDefinition;
      let preText = o[id];
      if (!_.isNil(preText)) {
        enText = preText;
      }
    }
    let finResult = formatResultWithReplacer(enText, ...args);
    return finResult;
  },
};

gutils.ExposureIt("TranslationUtils", TranslationUtils, true);

export default TranslationUtils;
export const Dot = TranslationUtils.Dot;
