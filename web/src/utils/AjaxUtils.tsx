import { logutils } from "./LogUtils";
import _ from "lodash";
import TranslationUtils, { Dot } from "./TranslationUtils";
import QS from "querystring";
import axios, { AxiosError, AxiosResponse } from "axios";
import gutils from "./GlobalUtils";
import { URL_PREFIX_LOCAL, URL_PREFIX_STATIC } from "../styles/config";
import devJson from "../static/dev.json";
import { AnyMapType } from "../styles/var";
import TokenUtils from "./TokenUtils";

interface CommonRequest {
  url: string;
  noHost?: boolean;
  isPOST?: boolean;
  querystring?: { [key: string]: any };
  data?: AnyMapType;
  host?: string;
  prefix?: string;
}
type AjaxPromise = Promise<AxiosResponse<any, any>>;

export type LocalRes = {
  error: AxiosError | null;
  response: AxiosResponse | null;
};

const AjaxUtils = {
  JSONtoFormData(obj): FormData {
    let formData = new FormData();
    _.forEach(obj, (x, d, n) => {
      formData.append(d, x);
    });
    return formData;
  },
  getHeaders: () => {
    return {
      "Content-Type": "application/json",
      "X-LOCAL-USER-LANG": TranslationUtils.CurrentLanguage,
      "X-LOCAL-USER-TOKEN": TokenUtils.getLocalUserToken(),
      "X-LOCAL-ADMIN-TOKEN": TokenUtils.getSystemInitToken(),
    };
  },
  getQSStr(obj: CommonRequest): string {
    let querystring = obj.querystring;
    return !_.isEmpty(querystring) ? "?" + QS.stringify(querystring) : "";
  },
  DoLocalRequest: async (obj: CommonRequest): Promise<LocalRes> => {
    let { url, isPOST, querystring, noHost, host = "", prefix = "" } = obj;
    if (gutils.IsDevMode() && noHost !== true) {
      // host = "http://localhost:8080";
    }
    if (!_.startsWith(url, "/")) {
      url = "/" + url;
    }
    url = URL_PREFIX_LOCAL + url;
    var formData = new FormData();
    _.forEach(obj.data, (x, d, n) => {
      if (!_.isNil(x)) {
        formData.append(d, x);
      }
    });
    try {
      let b = await axios({
        headers: AjaxUtils.getHeaders(),
        data: formData,
        method: isPOST ? "POST" : "GET",
        url: host + prefix + url + AjaxUtils.getQSStr(obj),
      });
      let data_errors = _.get(b, "data.errors");
      if (!_.isEmpty(data_errors)) {
        return {
          response: null,
          error: data_errors,
        };
      }
      return {
        response: b,
        error: null,
      };
    } catch (e) {
      let error = e as AxiosError;
      logutils.log("error", e);
      return {
        error: error,
        response: null,
      };
    }
  },
  GetStaticPrefix: () => {
    return URL_PREFIX_STATIC;
  },
  DoStaticRequest: (obj: CommonRequest): AjaxPromise => {
    let finalURL: string =
      AjaxUtils.GetStaticPrefix() + obj.url + AjaxUtils.getQSStr(obj);
    logutils.log("Do static request", obj, finalURL);
    return axios({
      headers: AjaxUtils.getHeaders(),
      method: obj.isPOST ? "POST" : "GET",
      url: finalURL,
      data: obj.data,
    });
  },
};

export default AjaxUtils;
