package com.denote.client.core

import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GData
import com.denote.client.utils.GHttpUtils
import com.denote.client.utils.GLogger

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InfraAPILogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        if (apiRequest.getActionType().equalsIgnoreCase("close")) {
            // direct exitfinish the load login
            GLogger.g().info("exited by the user's demands via calling the API")
            System.exit(-1)
        }
        if (apiRequest.getActionType().equalsIgnoreCase("get-download-status")) {
            def res = InfraKeys.DOWNLOAD_STATUS_MAP.get(apiRequest.param['UID']);
            return APIResponse.ok(res)
        }
        if (apiRequest.getActionType().equalsIgnoreCase("close-download")) {
            apiRequest.param['type'].each {
                GHttpUtils.removeHttpResponse(it as String)
            }
            return APIResponse.ok()
        }
        if (apiRequest.getActionType().equalsIgnoreCase("waiting-for-logs")) {
            def MSG_SOURCE = (apiRequest.param['MSG_SOURCE'] + '') as String
            def SIZE = ((apiRequest.param['SIZE'] + '') as String).toInteger()
            def CTN = ((apiRequest.param['CTN'] + '') as String).toInteger()
            def WAIT_COUNT_LATCH_FOR_NEW_LOGS = InfraKeys.WAIT_COUNT_LATCH_FOR_NEW_LOGS;
            int tryTimes = 0;
            synchronized (MSG_SOURCE.toString().intern()) {
                APIResponse finCallPageResult = null;
                def localGetRetrieveFunc = {
                    Map<String, Object> mymap = [:]
                    mymap['pageInfo'] = [pageIndex: 1,
                                         pageSize : 0]
                    mymap.putAll(apiRequest.param['featureArgs'] as Map)
                    finCallPageResult = GlobalController.call(InfraAPILogicFunc.class, new APIRequest("infra",
                            "logging-query-by-page",
                            mymap))
                    if (finCallPageResult.resOk()) {
                        def pageCount = finCallPageResult.content.pageCount
                        if ('' + pageCount != CTN + '') {
                            return true
                        }
                    }
                    return false
                }
                while (true) {
                    if (localGetRetrieveFunc()) {
                        break;
                    }
                    def countDownLatch = WAIT_COUNT_LATCH_FOR_NEW_LOGS.get(MSG_SOURCE);
                    if (countDownLatch == null || countDownLatch.getCount() == 0) {
                        countDownLatch = new CountDownLatch(1)
                        WAIT_COUNT_LATCH_FOR_NEW_LOGS.put(MSG_SOURCE, countDownLatch)
                    }
                    countDownLatch.await(20, TimeUnit.SECONDS)
                    tryTimes++
                    if (tryTimes >= 3) {
                        break;
                    }
                }
                localGetRetrieveFunc()
                def res_pageCount = finCallPageResult.content.pageCount as Integer
                def lastPage = Math.ceil(res_pageCount / SIZE)
                return APIResponse.ok([pageSize : SIZE,
                                       pageIndex: lastPage,
                                       pageCount: res_pageCount])
            }
        }

        def preResultForConfig = GData.runTemplateUpdate(apiRequest, "logging-", "G_INFRA_LOGGING");
        if (preResultForConfig.resOk()) {
            return preResultForConfig;
        }
        return APIResponse.noSolution();
    }
}
