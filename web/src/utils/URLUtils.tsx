import _ from "lodash";
import gutils from "./GlobalUtils";
import staticDevJson from "../static/dev.json";
import { useHistory } from "react-router";
import { Dot } from "./TranslationUtils";
import { useEffect } from "react";

class Header {
  Name: string = "";
  Value: string = "";
}
class Request {
  Inited: boolean = false;
  UsingHTTPSProtocol: boolean = false;
  Host: string = "";
  Port: number = -1;
  Token: string = "";
  BaseCtxAPI: string = "";
}
const VAL_REQUEST_OBJ: Request = {
  Inited: false,
  UsingHTTPSProtocol: false,
  Host: "",
  Port: -1,
  Token: "",
  BaseCtxAPI: "",
};
const URLUtils = {
  useUpdateTitle(title, eff: string[]) {
    useEffect(() => {
      let newTitle = "";
      if (gutils.empty(title)) {
        newTitle = Dot("5srFq", "CodeGen ToolBox");
      } else {
        newTitle = title + " - " + Dot("n03k0", "CodeGen ToolBox");
      }
      if (document.title != newTitle) {
        document.title = newTitle + "";
      }
    }, [title, ...eff]);
  },
  GetRoutePath(subPath: string): string {
    return "/app" + subPath;
  },
  UpdateRequestObj: (newRequestObj: Request) => {
    _.merge(VAL_REQUEST_OBJ, newRequestObj);
  },
  GetBaseURL: (): string => {
    return `${VAL_REQUEST_OBJ.UsingHTTPSProtocol ? `http` : `https`}://${
      VAL_REQUEST_OBJ.Host
    }:${VAL_REQUEST_OBJ.Port}${VAL_REQUEST_OBJ.BaseCtxAPI}`;
  },
  GetHeaders: (): Header[] => {
    return [];
  },
  _Init: async () => {
    if (gutils.IsDevMode()) {
      URLUtils.UpdateRequestObj({
        Inited: true,
        UsingHTTPSProtocol: false,
        Host: "http",
        Port: 8080,
        Token: staticDevJson.token,
        BaseCtxAPI: "",
      });
    } else {
      // will not process
    }
  },
};

export default URLUtils;
