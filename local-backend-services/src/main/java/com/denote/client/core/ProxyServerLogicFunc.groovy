package com.denote.client.core

import com.denote.client.concurrent.GenerateAssignCentre
import com.denote.client.concurrent.MissionDispatchCentre
import com.denote.client.config.other.ProxyServlet
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.extra.CommonMissile
import com.denote.client.handler.extra.ProxyServerMissile
import com.denote.client.utils.GData

class ProxyServerLogicFunc extends BasicLogicFunc {
    public static final String KEY_EXTRA_DATA_PROXY_RULES = 'EXTRA_DATA_PROXY_RULES';
    public static final String KEY_EXTRA_DATA_PROXY_RULES_PATH_REWRITE = 'EXTRA_DATA_PROXY_RULES_PATH_REWRITE'

    @Override
    APIResponse handle(APIRequest apiRequest) {
        if (apiRequest.getActionType() == 'test') {
            return APIResponse.ok('test func')
        }
        if (apiRequest.getActionType() == 'config-delete') {
            def temp_id = apiRequest.param['ID'] as Integer
            def crtConfig = GData.g().queryFirst("select * from g_proxy_config where id=:ID", [ID: temp_id])
            if (crtConfig['RUN_STATUS'] != 0) {
                return APIResponse.err(-1, "Please stop the server at first before deleting it.", null)
            }
        }
        if (apiRequest.getActionType() == 'config-upset') {
            def crtParam = apiRequest.param;
            def waitConfigObject = [:]
            crtParam.eachWithIndex { Map.Entry<String, Object> entry, int idx ->
                def configKey = entry.getKey();
                if (!configKey.startsWith("EXTRA")) {
                    def configValue = entry.getValue()
                    waitConfigObject[configKey] = configValue
                }
            }
            // add server
            def res_for_server = GData.modify("g_proxy_config", "upset", waitConfigObject, true)
            def configID = waitConfigObject['ID'];
            if (configID == null) {
                configID = res_for_server.content['ID'] as Integer
            }
            // add rule
            List<Map<String, Object>> proxyRulesList = (List<Map<String, Object>>) apiRequest.param['EXTRA_DATA_PROXY_RULES']
            if (proxyRulesList != null) {
                // delete no-need server
                def crtRulesID = proxyRulesList.collect({ x -> x['ID'] }).findAll({ x -> x != null })
                if (crtRulesID != null) {
                    GData.g().exec("delete from g_proxy_config_rule where config_id in (${configID}) and id not in (${crtRulesID.join(',')})")
                }


                proxyRulesList.collect({ eachRule ->
                    def newEachRuleMap = [:]
                    newEachRuleMap['CONFIG_ID'] = configID
                    eachRule.each {
                        if (!it.getKey().toString().startsWith("EXTRA")) {
                            newEachRuleMap[it.getKey()] = it.getValue()
                        }
                    }

                    def res_for_config_rule = GData.modify("g_proxy_config_rule", "upset", newEachRuleMap, true)

                    if (res_for_config_rule != null) {

                        def config_rule_id = newEachRuleMap['ID'];
                        if (config_rule_id == null) {
                            config_rule_id = res_for_config_rule.content['ID'] as Integer
                        }

                        // add rule path rewrites
                        List<Map<String, Object>> proxyRulesPathRewrites = (List<Map<String, Object>>) eachRule['EXTRA_DATA_PROXY_RULES_PATH_REWRITE']

                        // delete unuse server
                        def crtRulesPathRewritesID = proxyRulesPathRewrites.collect({ x -> x['ID'] }).findAll({ x -> x != null })
                        if (crtRulesPathRewritesID != null) {
                            GData.g().exec("delete from g_proxy_config_rules_path_rewrite where config_rule_id in (${config_rule_id}) and id not in (${crtRulesPathRewritesID.join(',')})")
                        }

                        proxyRulesPathRewrites.each { eachPathRewrites ->
                            def newEachPathRewrite = [:];
                            newEachPathRewrite.putAll(eachPathRewrites)
                            newEachPathRewrite['CONFIG_RULE_ID'] = config_rule_id;
                            def res_addRulePathRewritesRes = GData.modify("g_proxy_config_rules_path_rewrite", "upset", newEachPathRewrite, true)
                        }
                    }
                })
            }
            return APIResponse.ok();
        }


        def preResultForFolder = GData.runTemplateUpdate(apiRequest, "folder-", "g_proxy_folder");
        if (preResultForFolder.resOk()) {
            return preResultForFolder;
        }
        def preResultForConfig = GData.runTemplateUpdate(apiRequest, "config-", "g_proxy_config");
        if (preResultForConfig.resOk()) {
            return preResultForConfig;
        }
        switch (apiRequest.getActionType()) {
            case "folder-list":
                return APIResponse.ok(GData.g().query("""
    select * from g_proxy_folder
"""));
            case "folder-detail":
                def map = ["fid": apiRequest.param["fid"]]
                return APIResponse.ok(GData.g().query("""
    select * from g_proxy_config where folder_id = :fid
""", map));
                break;
            case "interrupt-process":
                // cancel submitted thread
                def missile = MissionDispatchCentre.get(MissionDispatchCentre.GROUP_KEY_PROXY_SERVER, "" + apiRequest.param["ID"])
                if (missile) {
                    CommonMissile serverExecutorThread = missile.getExecuteBusinessRunnable() as CommonMissile;
                    serverExecutorThread.getTomcatInstance().shutdown();
                }
                GData.g().exec("""
update g_proxy_config set run_status=ifnull(pre_run_status,0) where id=:ID
""", ["ID": apiRequest.param["ID"]])
                break;
            case "opt-machine":
                def updateStatusForServer = { newSt ->
                    GData.modify("g_proxy_config", "update", ["ID"        : apiRequest.param["ID"],
                                                              "RUN_STATUS": newSt])
                }
                def stopServerFunc = {
                    def missile = MissionDispatchCentre.get(MissionDispatchCentre.GROUP_KEY_PROXY_SERVER, "" + apiRequest.param["ID"])
                    if (missile) {
                        missile.kill();
                    }

                    def crtRulesList = GData.g().query("select * from g_proxy_config_rule where config_id=:ID", [ID: apiRequest.param["ID"]])
                    crtRulesList.each { eachRule ->
                        def eachRuleId = eachRule["ID"] as Integer
                        def crtProxyMainLogicServlet = GenerateAssignCentre.PROXY_SERVLET_MAPPING_BY_RULE_ID.get(eachRuleId)
                        if (crtProxyMainLogicServlet != null) {
                            crtProxyMainLogicServlet.destroy()
                            GenerateAssignCentre.PROXY_SERVLET_MAPPING_BY_RULE_ID.remove(eachRuleId)
                        }
                    }
                    updateStatusForServer(0)
                }
                def optType = apiRequest.param["optType"]
                def nextRunStatus = null;
                switch (optType) {
                    case "start":
                        // to 1
                        nextRunStatus = 1;
                        break;
                    case "stop":
                        // to 0
                        nextRunStatus = 0;
                        break;
                    case "restart":
                        // to 0, then to 1
                        stopServerFunc();
                        nextRunStatus = 1;
                        break;
                }

                // check no duplicate boot
                def ID = apiRequest.param["ID"]
                def crtProxyConfig = GData.g().queryFirst("select * from g_proxy_config where id=:ID", [ID: ID])
                if (crtProxyConfig['RUN_STATUS'] as Integer == 1 && optType == 'start') {
                    return APIResponse.err("The server has been started up before, cannot trigger the same action again.")
                }

                GData.g().exec("""
update g_proxy_config set pre_run_status=run_status,run_status=5,next_run_status=:NEXT_VAL where id=:ID
""", ["ID"      : ID,
      "NEXT_VAL": nextRunStatus]);
                if (nextRunStatus == 0) {
                    stopServerFunc();
                } else {
                    def launch = MissionDispatchCentre.launch(MissionDispatchCentre.GROUP_KEY_PROXY_SERVER, "" + apiRequest.param["ID"],
                            (new ProxyServerMissile(apiRequest,
                                    new ProxyServlet()
                            )));
                    return launch.getSysRes()
                }
                break;
            default:
                return APIResponse.err(-1, "no relevant solution", null);
        }
        return APIResponse.ok()
    }

}
