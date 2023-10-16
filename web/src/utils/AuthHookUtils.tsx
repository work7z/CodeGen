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
import exportUtils from "./ExportUtils";
import UserSlice, { UserConfig } from "../slice/userSlice";
import apiSlice from "../slice/apiSlice";
import * as vars from "../styles/var";
import { useEffect } from "react";

export type AuthStatus = {
  isFetching: boolean;
  HasLogin: boolean;
  currentUser: UserConfig | null;
};

const AuthHookUtils = {
  vars,
  useQueryAuthStatus(): AuthStatus {
    let currentUserObj = exportUtils.useSelector((val) => {
      return {
        userConfig: val.user.currentUser,
        refreshTokenStatus: val.user.refreshTokenStatus,
      };
    });
    let userToken = TokenUtils.getLocalUserToken();
    const userObj = apiSlice.useGetUserObjByTokenQuery(
      {
        userToken: userToken || "",
        refreshTokenStatus: currentUserObj.refreshTokenStatus,
      },
      {
        refetchOnMountOrArgChange: true,
      }
    );
    const dis = exportUtils.dispatch();
    useEffect(() => {
      if (userObj.isSuccess && userObj.data.payload.value.Found) {
        dis(
          UserSlice.actions.updateUserObject({
            userConfig: userObj.data.payload.value.Obj,
          })
        );
      }
    }, [userObj]);

    if (gutils.empty(userToken)) {
      return {
        isFetching: false,
        HasLogin: false,
        currentUser: null,
      };
    }
    if (_.isNil(currentUserObj)) {
      return {
        isFetching: true,
        HasLogin: false,
        currentUser: null,
      };
    }
    return {
      isFetching: userObj.isFetching,
      HasLogin: userObj.isSuccess ? userObj.data.payload.value.Found : false,
      currentUser: currentUserObj.userConfig,
    };
  },
};

export default AuthHookUtils;
