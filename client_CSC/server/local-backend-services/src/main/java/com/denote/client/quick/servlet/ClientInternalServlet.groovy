package com.denote.client.quick.servlet

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.denote.client.config.WebMvcConfig
import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GLogger
import org.apache.commons.lang3.StringUtils

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

class ClientInternalServlet extends HttpServlet {
    private static GlobalController globalController = new GlobalController();
    private static Pattern pattern_url = Pattern.compile("/([^/]+)/([^/]+)");
    private static WebMvcConfig webMvcConfig = new WebMvcConfig();


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        def g = GLogger.g('client-internal')
        def sendError = { int code, String arg ->
            HttpServletResponse res = resp;
            res.setStatus(code)
            res.setCharacterEncoding("UTF-8");
            res.setHeader("Content-type", "application/json;charset=UTF-8");
            res.getWriter().write(JSON.toJSONString(APIResponse.err(arg)))
            res.getWriter().flush()
            res.getWriter().close()
        }
        def liveIdVal = req.getHeader("X-FE-LIVE-ID")
        if(liveIdVal != null && liveIdVal.length() != 0){
            InfraKeys.CRT_LIVE_ID = liveIdVal
        }
        try {
            HttpServletResponse res = resp;
            if (!req.getMethod().equalsIgnoreCase("POST")) {
                sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "The method does not supported");
                return;
            }
            // initialize parameter
            APIRequest apiRequest = new APIRequest();


            // matching
            def requestURI = req.getRequestURI()
            def matcher = pattern_url.matcher(requestURI)
            g.debug("receiving the URL ${requestURI}")
            def isFind = matcher.find()
            if (!isFind) {
                g.error("Cannot match the pattern for ${requestURI}")
                sendError(HttpServletResponse.SC_NOT_FOUND, "404 Not Found");
                return;
            }
            apiRequest.setActionCategory(matcher.group(1))
            apiRequest.setActionType(matcher.group(2))

            // interceptor and set common resp headers
            def isPass = webMvcConfig.getHandleAdaptor().preHandle(req, resp, null)
            if (!isPass) {
                g.error("Permission denied for ${requestURI}")
                sendError(401, "permission denied");
                return;
            }

            // initializing the post body
            def map = getPostBody(req)['param'] as Map
            if (map == null) {
                map = [:]
            }
            apiRequest.setParam(map)

            // handle action
            def action = globalController.action(apiRequest, apiRequest.getActionCategory(), apiRequest.getActionType())
            if (action.resOk()) {
                g.debug("200 OK for ${requestURI}", action)
                String response = JSON.toJSONString(action)
                res.getWriter().write((response))
                res.getWriter().flush()
                res.getWriter().close()
            } else {
                g.error("400 ERROR for ${requestURI}, message is " + JSON.toJSONString(action))
                sendError(HttpServletResponse.SC_BAD_REQUEST, action.getMessage());
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace()
            g.error("ClientInternalServlet error occurred", throwable);
            sendError(500, throwable.getMessage())
        }
    }


    public Map<String, String> getPostBody(HttpServletRequest req) {
        Map<String, String> temp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
        req.getParameterMap().each {
            def key = it.getKey()
            def value = it.getValue()
            def okval = null;
            if (value == null || value.length == 0) {
                okval = null
            } else {
                okval = value[0]
            }
            temp[StringUtils.trim(key)] = StringUtils.trim(okval)
        }
        try {
            def jsonText = req.getInputStream().getText("UTF-8")
            if (jsonText != null && jsonText.trim().startsWith("{")) {
                def finPutMap = JSONObject.parseObject(jsonText, Map.class)
                temp.putAll(finPutMap)
            }
        } catch (Throwable throwable) {
            // do nothing here
        }
        return temp;
    }


}
