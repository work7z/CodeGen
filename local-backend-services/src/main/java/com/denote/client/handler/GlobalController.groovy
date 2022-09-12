package com.denote.client.handler

import com.denote.client.constants.InfraKeys
import com.denote.client.core.*
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.DynamicUtils
import com.denote.client.utils.GUtils
import com.denote.client.utils.GlobalFlag
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import java.util.concurrent.TimeUnit

@Component
@RestController
class GlobalController {

    @PostMapping("/{actionCategory}/{actionType}")
    public APIResponse action(@RequestBody APIRequest apiRequest,
                              @PathVariable String actionCategory,
                              @PathVariable String actionType) {
        if (GlobalFlag.WAIT_INIT) {
            while (true) {
                if (InfraKeys.WAIT_SYSTEM_INIT.getCount() != 0) {
                    InfraKeys.WAIT_SYSTEM_INIT.wait(TimeUnit.SECONDS.toMillis(120))
                }
                if (!GlobalFlag.WAIT_INIT) {
                    break;
                } else {
                    Thread.sleep(5000);
                }
            }
        }
        if (!GUtils.isDevMode()) {
            call(InitializerLogicFunc.class, null);
        }
        apiRequest.setActionCategory(actionCategory);
        apiRequest.setActionType(actionType);
        def globalCategoryAndWorkFuncMapping = [
                "proxy"  : ProxyServerLogicFunc.class,
                "static" : StaticServerLogicFunc.class,
                "channel": MsgChannelLogicFunc.class,
                "system" : SystemAPILogicFunc.class,
                'quickly': QuickAccessLogicFunc.class,
                "infra"  : InfraAPILogicFunc.class,
                "dblink": DbLinkLogicFunc.class
        ]
        def workFuncClass = globalCategoryAndWorkFuncMapping[actionCategory]
        if (workFuncClass == null) {
            return APIResponse.err(-1, "cannot proceed your request", null);
        }
        return call(workFuncClass, apiRequest);
    }

    private static Set<String> onceMapRecorder = new HashSet<>();

    public static APIResponse callOnce(Class clz, APIRequest apiRequest) {
        def clzName = clz.getClass().getName();
        if (onceMapRecorder.contains(clzName)) {
            return null;
        } else {
            def response = call(clz, apiRequest)
            onceMapRecorder.add(clzName)
            return response
        }
    }

    public static APIResponse call(Class clz, APIRequest apiRequest) {
        try {
            if (GUtils.isDevMode()) {
                def runResult = DynamicUtils.run(clz, apiRequest)
                return runResult;
            }
            def instance = clz.newInstance();
            return instance.handle(apiRequest);
        } catch (Throwable e) {
            e.printStackTrace()
            println e.getMessage()
            throw e
        }
    }

}
