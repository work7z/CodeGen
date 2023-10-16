package com.denote.client.test


import com.denote.client.core.StaticServerLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GData

class LocalProxyServerTesting {
    static void main(String[] args) {
        GData.g().exec("update g_static_config set run_status=0 where 1=1")
        GlobalController.call(StaticServerLogicFunc.class, new APIRequest(
                "proxy",
                "opt-machine",
                [
                        "ID"     : 11,
                        "optType": "start"
                ]
        ))
    }
}
