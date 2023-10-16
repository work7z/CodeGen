import { logutils } from "./LogUtils";

import _ from "lodash";
import { Dot } from "./TranslationUtils";
import { AxiosError } from "axios";

const STR_DEV_MODE = "DEV_MODE";

function uuid(str = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx") {
  return str
    .replace(/[xy]/g, function (c) {
      var r = (Math.random() * 16) | 0,
        v = c == "x" ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    })
    .replace(/-/gi, "");
}
const CRT_PAGE_ID = uuid();

const gutils = {
  stopE(e: any) {
    if (_.isNil(e)) return;
    e.preventDefault();
    e.stopPropagation();
  },
  uuid,
  CRT_PAGE_ID,
  emptyArr(val) {
    return _.isEmpty(val);
  },
  empty(val: string | null, ...anyOtherValues: string[]): boolean {
    if (_.isNil(anyOtherValues)) {
      anyOtherValues = [];
    }
    for (let a of anyOtherValues) {
      if (a == val) {
        return true;
      }
    }
    if (_.isNil(val) || val == "") {
      return true;
    }
    return false;
  },
  getStaticPath(subPath: string): string {
    return `/static/${subPath}`;
  },
  ExposureIt(key: string, value: any, devVisibleOnly?: boolean) {
    if (devVisibleOnly === true && !gutils.IsDevMode()) {
      return;
    }
    _.set(window, key, value);
  },
  GetUserActualClientLang(): string {
    let locale_str = navigator.language;
    let finalLang = "en_US";
    if (locale_str == "zh-CN") {
      finalLang = "zh_CN";
    } else if (locale_str == "zh-TW" || locale_str == "zh-HK") {
      finalLang = "zh_HK";
    }
    return finalLang;
  },
  safeparse(str: string | null) {
    if (_.isNil(str)) {
      return null;
    }
    try {
      return JSON.parse(str);
    } catch (err) {
      return null;
    }
  },
  sleep(val: number): Promise<any> {
    return new Promise((e: any) => {
      setTimeout(() => {
        e();
      }, val);
    });
  },
  getWebErrMsg(e: any): string {
    if (e && e.data && e.data.errors) {
      return _.join(e.data.errors, ",");
    }
    if (_.isString(e)) {
      return e;
    }
    let st = _.get(e, "originalStatus");
    let data = _.get(e, "data");
    return `${st} -> ${data}`;
  },
  getErrAxiosMsg(e: any): string {
    if (_.isArray(e)) {
      return _.join(e, ",");
    }
    let errors = _.get(e.response?.data, "errors");
    let finMsg = !_.isNil(errors)
      ? _.join(errors, ",")
      : gutils.getErrMsg(e as Error);
    let r = `[${e.response?.status}] ${finMsg}`;
    return r;
  },
  getErrMsg(_e): string {
    let e = _e as Error;
    if (_.isNil(e)) {
      return Dot("YpsgR", "Unknown Error");
    }
    return e.message;
  },
  IsPortalMode(): boolean {
    return false;
  },
  SetDevMode: (val: boolean = true) =>
    localStorage.setItem(STR_DEV_MODE, val + ""),
  IsDevMode: (): boolean => localStorage.getItem(STR_DEV_MODE) == "true",
};

export default gutils;
