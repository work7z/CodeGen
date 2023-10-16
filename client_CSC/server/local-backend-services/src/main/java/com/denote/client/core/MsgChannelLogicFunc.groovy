package com.denote.client.core

import com.denote.client.concurrent.MsgChannelFlag
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData
import org.apache.commons.lang3.StringUtils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MsgChannelLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        // handle
        def preResultForConfig = GData.runTemplateUpdate(apiRequest, "msg-", "g_message_channel");
        if (preResultForConfig.resOk()) {
            return preResultForConfig;
        }

        def g = GData.g();
        if (MsgChannelLogicFunc)
            if (MsgChannelFlag.countDownLatchWhenNewData.getCount() <= 0) {
                MsgChannelFlag.countDownLatchWhenNewData = new CountDownLatch(MsgChannelFlag.COUNT_VALUE);
            }
        switch (apiRequest.getActionType()) {
            case "latest-new-msg-count":
                synchronized (MsgChannelLogicFunc.class) {
                    def ctnSQL = "select * from g_message_channel where has_read=0 ";
                    def ctn_not_read = g.ctn(ctnSQL);
                    def lastCount = apiRequest.param["lastCount"];
                    //  waiting for the messages until new data to be returned
                    if (ctn_not_read == 0 || (ctn_not_read == lastCount)) {
                        MsgChannelFlag.countDownLatchWhenNewData.await((60), TimeUnit.SECONDS);
                        ctn_not_read = g.ctn(ctnSQL)
                    }
                    return APIResponse.ok([
                            "count": ctn_not_read
                    ]);
                }
                break;
            case "send":
                // send
                def workfunc = ['error_info', 'ERROR_INFO']
                workfunc.each {
                    def error_info = (apiRequest.param[it]) as String
                    if (error_info != null) {
                        error_info = StringUtils.truncate(error_info as String, 100)
                        apiRequest.param[it] = error_info
                    }
                }
                g.modify("g_message_channel", "insert", apiRequest.param)
                MsgChannelFlag.countDownLatchWhenNewData.countDown()
                break;
            case "clean":
                g.exec("""
delete from g_message_channel 
""");
                break;
            case "have-it-read":
                g.exec("""
update g_message_channel set has_read=1 where create_time < now()
""");
                break;
            default:
                return APIResponse.noSolution();
                break;
        }
        return APIResponse.ok();
    }
}


