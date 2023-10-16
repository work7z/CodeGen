import _ from "lodash";
import gutils from "./GlobalUtils";
import staticDevJson from "../static/dev.json";
import { useHistory, useParams } from "react-router";
import URLUtils from "./URLUtils";

const RouteUtils = {
  getCompleteURL(str: string) {
    return URLUtils.GetRoutePath(str);
  },
  hist_ref: null,
  useHistory: useHistory,
  usePathVariablesList: () => {
    let hist = RouteUtils.useHistory();
    RouteUtils.hist_ref = hist as any;
    return hist.location;
  },
  getHistoryByHistRefDirect(): ReturnType<typeof useHistory> {
    return RouteUtils.hist_ref as unknown as ReturnType<typeof useHistory>;
  },
};

export default RouteUtils;
