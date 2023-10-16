package com.denote.client.core

import com.alibaba.fastjson.JSON
import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.TomcatInstanceWrapper
import com.denote.client.handler.GlobalController
import com.denote.client.handler.extra.CommonMissile
import com.denote.client.utils.GData
import com.denote.client.utils.GLogger
import com.denote.client.utils.GUtils
import org.apache.catalina.LifecycleException

class CommonMainStartServerLogicFunc extends BasicLogicFunc {


    public boolean isStatic(CommonMissile ref) {
        def doConfigTableName = ref.getConfigTableName()
        return doConfigTableName.toLowerCase().contains("static")
    }

    @Override
    APIResponse handle(APIRequest apiRequest) {
        CommonMissile ref = apiRequest.param['ref'] as CommonMissile;
        def doConfigTableName = ref.getConfigTableName()
        def servlet = ref.getHandleServlet();

        def g = GData.g()
        def crtConfigObj = ref.getCrtDataObj()
        String LOCAL_LISTEN_PORT = crtConfigObj['LOCAL_LISTEN_PORT'] as String;

        ref.removeLog(ref)

        try {
            def ctxPath = crtConfigObj['CONTEXT_PATH'] as String
            def listDir = (crtConfigObj['LIST_DIRECTORY'])
            def isLocalSSL = (crtConfigObj['IS_LOCAL_SSL'])
            def crtHost = crtConfigObj['LOCAL_LISTEN_IPADDR'] as String
            ref.doLog(ref, InfraKeys.LOG_TYPE_PRIMARY_0, "Starting Instance for the ${isStatic(ref) ? "static" : "proxy"} server, ID is ${crtConfigObj['ID']}")
            def stType = isStatic(ref)
            [
                    "Server Name: ${crtConfigObj['NAME']}",
                    "Server Description: ${crtConfigObj['BRIEF']}",
                    "Listen Port: ${LOCAL_LISTEN_PORT}",
                    stType ? "ContextPath: ${ctxPath}" : null,
                    stType ? "List Directory?: ${listDir == 1 ? 'Yes' : 'No'}" : null,
                    stType ? "Using SSL?: ${isLocalSSL == 1 ? 'Yes' : 'No'}" : null,
                    stType ? "Bind IP Address: ${crtHost}" : null,
            ].each {
                if (it != null) {
                    ref.doLog(ref, InfraKeys.LOG_TYPE_DEBUG_4, it.toString())
                }
            }
            ref.doLog(ref, InfraKeys.LOG_TYPE_INTERNAL_5, JSON.toJSONString(crtConfigObj))

            // starting boot new server
            TomcatInstanceWrapper tomcatInstance = (GlobalController.call(TomcatServerLogicFunc.class,
                    new APIRequest(["webPort"       : LOCAL_LISTEN_PORT,
                                    "contextPath"   : ctxPath,
                                    "httpServlet"   : servlet,
                                    "isLocalSSL"    : isLocalSSL,
                                    ref             : ref,
                                    'LIST_DIRECTORY': listDir,
                                    "host"          : crtHost])).content) as TomcatInstanceWrapper

            ref.setTomcatInstance(tomcatInstance)

            tomcatInstance.start()

            ref.doLog(ref, InfraKeys.LOG_TYPE_SUCCESS_1, "Booted on port(s) ${LOCAL_LISTEN_PORT}(${isLocalSSL == 1 ? 'https' : 'http'}).")
            ref.doLog(ref, InfraKeys.LOG_TYPE_SUCCESS_1, "Started the ${isStatic(ref) ? "static" : "proxy"} server successfully.")

            def startDate = Calendar.getInstance().getTime()

            ref.updateRunStatus(1);

            return APIResponse.ok(new Thread(new Runnable() {
                @Override
                void run() {
                    // waiting for shut down
                    tomcatInstance.waitAfterStarted()

                    ref.updateRunStatus(0)

                }
            }))
        } catch (Throwable throwable) {

            throwable.printStackTrace()
            GLogger.g().error("Failed while creating the static server", throwable)
            def error_info = throwable.getMessage();
            if (throwable.getClass() == LifecycleException.class) {
                if (ref.tomcatInstance != null) {
                    List<Throwable> allError = ref.tomcatInstance.getAllError();
                    if (allError && allError.get(0) && allError.get(0).getCause().class == BindException.class) {
                        error_info = allError.get(0).getCause().getMessage()
                    }
                    GLogger.g().error("port used error", allError, throwable)
                }
            }

            ref.updateRunStatus(2)
            def viewErrorMsg = error_info
            GlobalController.call(MsgChannelLogicFunc.class, new APIRequest('send', [intent_type : 'danger',
                                                                                     error_info  : error_info,
                                                                                     title       : "Failed to start \"${crtConfigObj['NAME']}\" up",
                                                                                     text_content: "Cause Reason: ${viewErrorMsg}"]))
            GData.g().exec("""
update ${doConfigTableName} set view_error_info=:MYVAL where id=:ID
""", [MYVAL: viewErrorMsg,
      ID   : ref.ID])

            ref.doLog(ref, InfraKeys.LOG_TYPE_DANGER_2, "Failed to start the ${isStatic(ref) ? "static" : "proxy"} server since the reason '${error_info}'")

            def fin_err = GUtils.getErrToStr(throwable)
            ref.doLog(ref, InfraKeys.LOG_TYPE_DANGER_DEBUG_4, fin_err)

            return APIResponse.err("Facing an error while booting the server: " + (error_info))
        }

    }


//    public static TomcatInstanceWrapper createTomcatInstanceForStaticUsage(HttpServlet httpServlet, String contextPath, String webPort, Object listDirectory, String host) {
//        def resp =
//        return resp.content
//    }

}
