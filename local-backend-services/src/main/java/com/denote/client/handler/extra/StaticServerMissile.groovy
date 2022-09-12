package com.denote.client.handler.extra

import com.denote.client.config.other.CommonServlet
import com.denote.client.dto.APIRequest

class StaticServerMissile extends CommonMissile {

    StaticServerMissile(APIRequest apiRequest, CommonServlet httpServlet) {
        super(apiRequest, "g_static_config", httpServlet)
    }

}
