import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import AjaxUtils from "../utils/AjaxUtils";
import _ from "lodash";
import { URL_PREFIX_LOCAL } from "../styles/config";
import gutils from "../utils/GlobalUtils";
import { url } from "inspector";
import { param } from "jquery";

let createNotProhibitedResources = (build, resName) => {
  return build.query({
    query: () => `/res/non-prohibited/${resName}`,
  });
};

export const apiSlice = createApi({
  baseQuery: fetchBaseQuery({
    // Fill in your own server starting URL here
    baseUrl: URL_PREFIX_LOCAL,
    prepareHeaders(headers, api) {
      let headers_New = AjaxUtils.getHeaders();
      _.forEach(headers_New, (x, d, n) => {
        if (!_.isNil(x)) {
          headers.set(d, x);
        }
      });
      return headers;
    },
    validateStatus: (response, result) => {
      gutils.ExposureIt("response_res", { response, result }, true);
      let errors = _.get(result, "errors");
      if (!_.isEmpty(errors)) {
        return false;
      }
      return response.status === 200 && !result.isError;
    },
  }),
  endpoints: (build) => ({
    // static
    getToolCategory: build.query({
      query: () => "/tool/category/list",
    }),
  }),
});

export interface ValueReq {
  InputText: string;
  InputFile: string;
  ExtraConfigMap?: Record<string, any>;
  ReturnAsFile?: boolean; // by default false
}

export interface ValueRes {
  Err?: Error;
  OutputText: string;
  OutputFile: string;
}

export interface ValueHandler {
  ConvertText: (req: ValueReq) => ValueRes;
  ConvertFile: (req: ValueReq) => ValueRes;
}

export type ExtensionFuncMap = Record<string, ValueHandler>;

type FormModel = Record<string, any>;

interface ExtensionAction {
  Id: string;
  Label: string;
  CallFuncList: string[];
}

export interface ExtensionVM {
  Layout: string;
  InitialFormModel?: FormModel;
  Info?: ExtensionInfo;
  Actions?: ExtensionAction[];
  FuncMap?: ExtensionFuncMap;
}

export default apiSlice;
export type ExtensionInfo = {
  Id: string;
  Label: string;
  Description: string;
};
export type ListExtForTheCategoryRes = {
  CategoryId: string;
  Id: string;
  Label: string;
  Icon: string;
  ChildrenAsInfo: ExtensionInfo[];
};
