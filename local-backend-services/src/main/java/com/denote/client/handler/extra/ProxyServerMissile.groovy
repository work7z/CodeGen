package com.denote.client.handler.extra

import com.denote.client.config.other.CommonServlet
import com.denote.client.config.proxy.ProxyMainLogicServlet
import com.denote.client.dto.APIRequest

class ProxyServerMissile extends CommonMissile {
    ProxyMainLogicServlet proxyMainLogicServlet;

    ProxyServerMissile(APIRequest apiRequest, CommonServlet httpServlet) {
        super(apiRequest, "g_proxy_config", httpServlet)
    }

}