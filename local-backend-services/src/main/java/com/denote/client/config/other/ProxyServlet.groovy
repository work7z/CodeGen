package com.denote.client.config.other

import com.denote.client.concurrent.WebHandleServletHolder
import com.denote.client.constants.InfraKeys
import com.denote.client.core.InternalProxyRunningLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GUtils

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ProxyServlet extends CommonServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        def config = this.getServletConfig()
        def servletConfig = getServletConfig()
        def ref = this.getRef()
        WebHandleServletHolder.SYS_CONFIG.set(config);
        WebHandleServletHolder.SYS_REQ.set(req);
        WebHandleServletHolder.SYS_RES.set(res);
        WebHandleServletHolder.SYS_REQ.set(req);
        WebHandleServletHolder.SYS_REF.set(this.getRef());
        try {
            GlobalController.call(InternalProxyRunningLogicFunc.class, new APIRequest([
                    "req"          : req,
                    "res"          : res,
                    "servletConfig": servletConfig,
                    "ref"          : ref
            ]))
        } catch (Throwable e) {
            ref.doLog(ref, InfraKeys.LOG_TYPE_DANGER_2, "Facing an error and cannot solve it, the error message is " + e.getMessage())
            def fin_err = GUtils.getErrToStr(e)
            ref.doLog(ref, InfraKeys.LOG_TYPE_DANGER_DEBUG_4, fin_err)
            throw e;
        } finally {
            WebHandleServletHolder.SYS_REQ.remove();
            WebHandleServletHolder.SYS_RES.remove();
            WebHandleServletHolder.SYS_REF.remove();
            WebHandleServletHolder.SYS_CONFIG.remove();
        }
    }

}
