package com.denote.client.core

import com.denote.client.concurrent.GenerateAssignCentre
import com.denote.client.config.proxy.ProxyMainLogicServlet
import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.extra.ProxyServerMissile
import com.denote.client.utils.GData

import java.util.regex.Pattern

class InternalProxyRunningLogicFunc extends CommonServiceImplLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        /**
         * hereby I want to clarify the usage of code
         * 1, retrieved all of these config_rules records by config_id
         * 2, matching the one whose path is matched with current pathname
         * 3, initializing or utilizing its extant proxy logic servlet instance
         * 4, handle its relevant proxy tasks*/
        ProxyServerMissile ref = apiRequest.param['ref'] as ProxyServerMissile
        def req = apiRequest.getReq();
        def res = apiRequest.getRes()
        def reqURI = req.getRequestURI()

        // get data obj and data id
        def crtDataObj = ref.getCrtDataObj();
        def config_id = crtDataObj['ID'] as Integer;


        // get all rules by config id
        def rulesList = GData.g().query("select * from g_proxy_config_rule where config_id=${config_id}")

        doLogInternalWithNoCode(InfraKeys.LOG_TYPE_PRIMARY_0, "start matching the first qualified proxy rule within these ${rulesList.size()} rules")

        def crtWorkProxyRule = null;
        int idx = 0;
        rulesList.find({ it ->
            idx++;
            def config_rule_id = it['ID']
            def configProxyRules = GData.g().query("select * from g_proxy_config_rules_path_rewrite where ifnull(disable,0) = 0 and  config_rule_id = '${config_rule_id}'")
            def isProxyCurrentRuleMatched = false;
            doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "checking the proxy rule '${it['RULE_NAME']}' and retrieving its ${configProxyRules.size()} path rewrite records")
            def matchedPathRewrites = configProxyRules.findAll({ eachRule ->
                def from_url_pattern = Pattern.compile(eachRule['FROM_URL_PATTERN'] as String);
                def matcher = from_url_pattern.matcher(reqURI)
                def isFind = matcher.find()
                doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "comparing the value between pattern ${from_url_pattern} and request URI ${reqURI} via regex matcher")
                if (!isProxyCurrentRuleMatched && isFind) {
                    doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "Matched! found a proxy rule that qualified with current req URI via having matched its URL pattern")
                    isProxyCurrentRuleMatched = true
                } else {
                    doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "Not Matched. skip the path rewrite record which isn't qualified.")
                }
                return isFind
            })
            // get first rule if matched
            if (isProxyCurrentRuleMatched && matchedPathRewrites.size() != 0) {
                it['rules'] = matchedPathRewrites
                crtWorkProxyRule = it
                doLogInternalWithNoCode(InfraKeys.LOG_TYPE_SUCCESS_1, "The proxy server will use ${it['RULE_NAME']}(ID: ${it['ID']}) as its proxy rule since its path rewrite config is matched with request URI.")
                return true;
            } else {
                doLogInternalWithNoCode(InfraKeys.LOG_TYPE_PRIMARY_0, "skipping the proxy rule record ${it['RULE_NAME']}(ID: ${it['ID']})")
//                doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DANGER_2, "The proxy server cannot find any proxy rule since there's no qualified path rewrite config is matched with request URI.")
            }
            return false
        })

        if (crtWorkProxyRule == null && rulesList.size() == 0) {
            flushText(res, 500, "please check the proxy rules since system found its empty while handling the request.")
            return APIResponse.noSolution()
        }

        if (crtWorkProxyRule == null && rulesList.size() != 0) {
            doLogInternalWithNoCode(InfraKeys.LOG_TYPE_WARNING_3, "The proxy server cannot find any proxy rule since there's no qualified path rewrite config is matched with request URI.")
            doLogInternalWithNoCode(InfraKeys.LOG_TYPE_WARNING_3, "As the above reason, the proxy server will use the first rule as default for the sake of proceeding its proxy request.")
            crtWorkProxyRule = rulesList.get(0)
        }

        def CRT_RULE_ID = crtWorkProxyRule['ID'] as Integer

        ProxyMainLogicServlet internalProxyServlet =
                GenerateAssignCentre.PROXY_SERVLET_MAPPING_BY_RULE_ID.getOrDefault(CRT_RULE_ID, new ProxyMainLogicServlet())
        GenerateAssignCentre.PROXY_SERVLET_MAPPING_BY_RULE_ID.put(CRT_RULE_ID, internalProxyServlet)

        internalProxyServlet.setCrtServletConfig(apiRequest.getConfig())

        // init param
        Map<String, String> initParam = ProxyMainLogicServlet.INIT_PARAM.get();
        initParam.put(ProxyMainLogicServlet.P_TARGET_URI, crtWorkProxyRule['DEST_HOST'] as String)
        doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "Initializing these config by the qualified rule...")
        def colMapFromDbToConfig = [[ProxyMainLogicServlet.P_PRESERVEHOST]       : 'IS_CHANGE_ORIGIN',
                                    [ProxyMainLogicServlet.P_PRESERVECOOKIES]    : 'KEEP_COOKIES',
                                    [ProxyMainLogicServlet.P_READTIMEOUT]        : 'READ_TIMEOUT',
                                    [ProxyMainLogicServlet.P_CONNECTTIMEOUT]     : 'CONNECT_TIMEOUT',
                                    [ProxyMainLogicServlet.P_MAXCONNECTIONS]     : 'MAX_CONNECTION',
                                    [ProxyMainLogicServlet.P_HANDLECOMPRESSION]  : 'HANDLE_COMPRESS',
                                    [ProxyMainLogicServlet.P_HANDLEREDIRECTS]    : 'HANDLE_REDIRECT',
                                    [ProxyMainLogicServlet.P_FORWARDEDFOR]       : 'FORWARD_IP',
                                    [ProxyMainLogicServlet.P_USESYSTEMPROPERTIES]: 'USE_SYSTEM_PROPERTIES']
        colMapFromDbToConfig.each {
            def finkey = it.getKey() as String;
            def finvalue = crtWorkProxyRule[it.getValue()] as Integer == 1 ? "true" : "false";
            initParam[finkey] = finvalue;
            doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "${finkey}: ${finvalue}")
        }

        // rewrite path by rule_id
        def pathRewriteRules = GData.g().query("select * from g_proxy_config_rules_path_rewrite where ifnull(disable,0) = 0 and config_rule_id=${config_id}")
        String finalRewroteReqURI = apiRequest.getReq().getRequestURI()
        String finalPathInfo = apiRequest.getReq().getPathInfo()
        doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "replacing by the ${pathRewriteRules.size()} path rewrite records...")
        pathRewriteRules.each { eachRulePathRewrite ->
            def FROM_URL_PATTERN = eachRulePathRewrite['FROM_URL_PATTERN'] as String
            def TO_URL_PATTERN = eachRulePathRewrite['TO_URL_PATTERN'] as String
            def beforePathInfo = finalPathInfo
            finalRewroteReqURI = finalRewroteReqURI.replaceFirst(FROM_URL_PATTERN, TO_URL_PATTERN)
            finalPathInfo = finalPathInfo.replaceFirst(FROM_URL_PATTERN, TO_URL_PATTERN)
            doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "replaced by the path rewrite record, FROM ${FROM_URL_PATTERN} TO ${TO_URL_PATTERN}, before replacing: '${beforePathInfo}' after replacing: '${finalPathInfo}'")
        }

        internalProxyServlet.setFinalReqURI(finalRewroteReqURI)
        internalProxyServlet.setFinalPathInfo(finalPathInfo)

        doLogInternalWithNoCode(InfraKeys.LOG_TYPE_DEBUG_4, "According to these path rewrite records, the request will be redirected to the URL ${finalPathInfo}")

        // start serving user proxy request after having rewrote rule_id
        internalProxyServlet.init()
        internalProxyServlet.defineDolog({ int msgType, String msg ->
            ref.doLog(ref, msgType, msg)
        })
        internalProxyServlet.service(apiRequest.getReq(), apiRequest.getRes())

        doLogInternalWithNoCode(InfraKeys.LOG_TYPE_PRIMARY_0, "Finished the proxy request.")

        return APIResponse.ok()
    }
}
