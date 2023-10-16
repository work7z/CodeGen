package com.denote.client.core

import com.denote.client.concurrent.MissionDispatchCentre
import com.denote.client.config.other.StaticServlet
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.extra.CommonMissile
import com.denote.client.handler.extra.StaticServerMissile
import com.denote.client.utils.GData

class StaticServerLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        if (apiRequest.getActionType() == 'config-delete') {
            def temp_id = apiRequest.param['ID'] as Integer
            def crtConfig = GData.g().queryFirst("select * from g_static_config where id=:ID", [ID: temp_id])
            if (crtConfig['RUN_STATUS'] != 0) {
                return APIResponse.err(-1, "Please stop the server at first before deleting it.", null)
            }
        }

        def preResultForFolder = GData.runTemplateUpdate(apiRequest, "folder-", "g_static_folder");
        if (preResultForFolder.resOk()) {
            return preResultForFolder;
        }
        def preResultForConfig = GData.runTemplateUpdate(apiRequest, "config-", "g_static_config");
        if (preResultForConfig.resOk()) {
            return preResultForConfig;
        }
        switch (apiRequest.getActionType()) {
            case "folder-list":
                return APIResponse.ok(GData.g().query("""
    select * from g_static_folder
"""));
            case "folder-detail":
                def map = ["fid": apiRequest.param["fid"]]
                return APIResponse.ok(GData.g().query("""
    select * from g_static_config where folder_id = :fid
""", map));
                break;
            case "interrupt-process":
                // cancel submitted thread
                def missile = MissionDispatchCentre.get(MissionDispatchCentre.GROUP_KEY_STATIC_SERVER, "" + apiRequest.param["ID"])
                if (missile) {
                    CommonMissile serverExecutorThread = missile.getExecuteBusinessRunnable() as CommonMissile;
                    serverExecutorThread.getTomcatInstance().shutdown();
                }
                GData.g().exec("""
update g_static_config set run_status=ifnull(pre_run_status,run_status) where id=:ID
""", ["ID": apiRequest.param["ID"]])
                break;
            case "opt-machine":
                def updateStatusForServer = { newSt ->
                    GData.modify("g_static_config", "update", ["ID"        : apiRequest.param["ID"],
                                                               "RUN_STATUS": newSt])
                }
                def stopServerFunc = {
                    def missile = MissionDispatchCentre.get(MissionDispatchCentre.GROUP_KEY_STATIC_SERVER, "" + apiRequest.param["ID"])
                    if (missile) {
                        missile.kill();
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
                def crtProxyConfig = GData.g().queryFirst("select * from g_static_config where id=:ID", [ID: ID])
                if (crtProxyConfig['RUN_STATUS'] as Integer == 1 && optType == 'start') {
                    return APIResponse.err("The server has been started up before, cannot trigger the same action again.")
                }

//                GData.g().exec("""
//update g_static_config set pre_run_status=run_status,run_status=5,next_run_status=:NEXT_VAL where id=:ID
//""", ["ID"      : apiRequest.param["ID"],
//      "NEXT_VAL": nextRunStatus]);

                if (nextRunStatus == 0) {
                    stopServerFunc();
                } else {
                    def launch = MissionDispatchCentre.launch(MissionDispatchCentre.GROUP_KEY_STATIC_SERVER, "" + apiRequest.param["ID"], (
                            new StaticServerMissile(
                                    apiRequest,
                                    new StaticServlet()
                            )
                    ));
                    return launch.sysRes
                }
                break;
            default:
                return APIResponse.err(-1, "no relevant solution", null);
        }
        return APIResponse.ok()
    }
}










