package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils

class SystemAPILogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        if ("health-check".equals(apiRequest.getActionType())) {
            return APIResponse.ok()
        }
        if ("dev-mode".equals(apiRequest.getActionType())) {
            return APIResponse.ok(GUtils.isDevMode())
        }
        if ("setting-restore".equals(apiRequest.getActionType())) {
            GData.g().exec("update G_SYS_SETTING set myvalue=fact_value where 1=1 ")
        }
        if ("setting-save-all".equals(apiRequest.getActionType())) {
            def allParam = apiRequest.param
            allParam.each {
                def key = it.getKey().toString()
                def value = Objects.toString(it.getValue())
                if (value == 'null') {
                    value = null
                }
                GData.g().exec("update g_sys_setting set myvalue=:VAL where mykey=:KEY", [
                        KEY: key,
                        VAL: value
                ])
            }
            return APIResponse.ok()
        }
        def loopArr = [
                [
                        "setting-",
                        "g_sys_setting"
                ],
                [
                        "shortcut-",
                        "g_sys_shortcut"
                ],
                [
                        "user-cache-",
                        "g_sys_user_cache"
                ]
        ]
        def finReturn = GData.runBatchTemplateChecking(loopArr, apiRequest)
        if (finReturn.resOk()) {
            return finReturn;
        }
        switch (apiRequest.getActionType()) {
            case "net-cards":
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                List<Map> list = [];
//                list.add([
//                        'label': 'default(127.0.0.1)',
//                        'value': '127.0.0.1'
//                ])
                def isGotAddr = false;
                for (NetworkInterface netint : Collections.list(nets)) {
                    def addresses = netint.getInetAddresses()
                    while (addresses.hasMoreElements()) {
                        def netAddr = addresses.nextElement()
                        netAddr.toString()
                        if (!netAddr.getHostAddress().contains(":")) {
                            if (netAddr.getHostAddress().toString() == '127.0.0.1') {
                                isGotAddr = true
                            }
                            list.add([
                                    'label': "${netint.getDisplayName().toString()}(${netAddr.getHostAddress().toString()})".toString(),
                                    'value': netAddr.getHostAddress().toString()
                            ])
                        }
                    }
                }
                if (!isGotAddr) {
                    list.add([
                            label: 'Default(127.0.0.1)',
                            value: '127.0.0.1'
                    ])
                }
                return APIResponse.ok(list)
                break;
        }
        return APIResponse.ok();
    }

}
