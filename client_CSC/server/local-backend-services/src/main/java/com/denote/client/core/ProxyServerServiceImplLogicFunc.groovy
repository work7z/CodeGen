package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.extra.CommonMissile
import org.apache.http.client.HttpClient

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ProxyServerServiceImplLogicFunc extends CommonServiceImplLogicFunc {

    @Override
    APIResponse handle(APIRequest apiRequest) {
        super.beforeHandle(apiRequest)
        def req = apiRequest.param["req"] as HttpServletRequest
        def res = apiRequest.param['res'] as HttpServletResponse
        def ref = apiRequest.param['ref'] as CommonMissile

        HttpClient proxyClient;

        flushText(res, 200, 'ok')

        return null
    }
}

