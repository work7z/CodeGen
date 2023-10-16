package com.denote.client.core

import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GData

import java.sql.Connection

class VacuumCleanerAndStartUpLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        GData.g().exec("update g_dblink_connections set is_connection = 0")
        GData.g().exec("update g_dblink_connections_scripts set activity_id = 0")

        ['static', 'proxy'].each { px ->
            def tableName = 'g_' + px + '_config'
            // reinstating all of these run_status in table g_static_config
            GData.g().exec("""
update ${tableName} set run_status=0,pre_run_status=0,next_run_status=0 where 1=1 
""")
            // auto boot these static server that being configured
            GData.g().query("""
select ID from ${tableName} where boot_flag = 1 order by id desc
""").each({
                def nextID = it['ID']
                GlobalController.call(px == 'static' ? StaticServerLogicFunc.class : ProxyServerLogicFunc.class, new APIRequest(px, "opt-machine", [
                        ID     : nextID,
                        optType: 'start'
                ]))
            })
        }

        // set infra boot record
        def addBootRecordParam = [
                VERSION: InfraKeys.VERSION,
                PUID   : InfraKeys.INFRA_P_UID,
        ]
        GData.modify("g_infra_boot_record", "insert", addBootRecordParam, true)
        InfraKeys.BOOT_RECORD_ID = addBootRecordParam["ID"] as Integer
        InfraKeys.updateLatestActiveTime()

        InfraKeys.WAIT_SYSTEM_INIT.countDown()
        return APIResponse.ok();
    }
}
