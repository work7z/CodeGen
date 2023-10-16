package com.denote.client.core

import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GUtils
import com.denote.client.utils.GlobalFlag

import javax.servlet.http.HttpServletRequest

class AuthLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        if (GlobalFlag.SYS_ALLOW_TOKEN == null) {
            getTheKeyToken()
        }
        HttpServletRequest req = apiRequest.param["req"]
        def idFromFe = req.getHeader("X-FE-RUID")
        if (GlobalFlag.SYS_ALLOW_TOKEN == idFromFe || InfraKeys.INFRA_P_UID == idFromFe) {
            return APIResponse.ok();
        } else {
            return APIResponse.err(-1, "keyfile mismatch", idFromFe)
        }
    }

    public static String getTheKeyToken() {
        def keyfile = new File(GUtils.getAppHomeDir(), ".key4denote");
        if (keyfile.exists()) {
            def keyToken = keyfile.readLines().join("").trim()
            GlobalFlag.SYS_ALLOW_TOKEN = keyToken
        } else {
            throw new RuntimeException("keyfile not exists");
        }
        return GlobalFlag.SYS_ALLOW_TOKEN;
    }
}
