import _ from "lodash";
import exportUtils from "./ExportUtils";
import ALL_NOCYCLE from "../nocycle";
import UserSlice from "../slice/userSlice";
const SYSTEM_INIT_TOKEN_LOCAL_KEY = "LOCAL_KEY_SYSTEM_INIT";
const USER_TOKEN_LOCAL_KEY = "LOCAL_KEY_USER_TOKEN";

const TokenUtils = {
  // system
  getSystemInitToken() {
    return localStorage.getItem(SYSTEM_INIT_TOKEN_LOCAL_KEY);
  },
  clearSystemToken() {
    localStorage.removeItem(SYSTEM_INIT_TOKEN_LOCAL_KEY);
  },
  setSystemInitToken(str: string) {
    localStorage.setItem(SYSTEM_INIT_TOKEN_LOCAL_KEY, str);
  },
  // local
  getLocalUserToken() {
    return localStorage.getItem(USER_TOKEN_LOCAL_KEY);
  },
  clearLocalUserToken() {
    localStorage.removeItem(USER_TOKEN_LOCAL_KEY);
  },
  setLocalUserToken(str: string) {
    localStorage.setItem(USER_TOKEN_LOCAL_KEY, str);
    setTimeout(() => {
      ALL_NOCYCLE.store?.dispatch(UserSlice.actions.updateTokenStatus());
    });
  },
};

// verify if its token mode
setTimeout(() => {
  let b = window.location.href.match(/t=([A-Za-z0-9]+)/);
  if (!_.isNil(b) && b[1]) {
    let systemToken = _.trim(b[1]);
    let hasEntry = window.location.href.indexOf("/app/entry") != -1;
    if (hasEntry && systemToken) {
      TokenUtils.setSystemInitToken(systemToken);
    }
  }
});

export default TokenUtils;
