package com.denote.client.handler.extra

import com.denote.client.concurrent.MissileRunnable
import com.denote.client.config.other.CommonServlet
import com.denote.client.constants.InfraKeys
import com.denote.client.core.CommonMainStartServerLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.InfraLog
import com.denote.client.dto.TomcatInstanceWrapper
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GData

abstract class CommonMissile implements MissileRunnable {
    String ID;
    String SOURCE;
    String configTableName;
    APIRequest apiRequest;
    CommonServlet handleServlet;
    Map<String, Object> crtDataObj;

    protected CommonMissile(APIRequest apiRequest, String configTableName, CommonServlet handleServlet) {
        this.apiRequest = apiRequest
        this.ID = "" + apiRequest.param['ID']
        this.SOURCE = (apiRequest.param.getOrDefault('SOURCE', 'system'));
        this.configTableName = configTableName
        this.handleServlet = handleServlet
        Map<String, Object> crtDataObj = GData.g().queryFirst("select * from ${configTableName} where id=:ID", [ID: this.ID])
        handleServlet.setCrtDataObj(crtDataObj)
        this.crtDataObj = crtDataObj
        this.handleServlet.setRef(this)
    }

    def updateRunStatus(int run_status) {
        GData.g().exec("""
update ${configTableName} set run_status=:VAL where id=:ID
""", [ID : this.ID,
      VAL: run_status]
        )
        if (run_status == 1) {
            GData.g().exec("""
update ${configTableName} set start_running_solid_time=now() where id=:ID
""", [ID: this.ID])
        }
    }
    TomcatInstanceWrapper tomcatInstance = null;

    TomcatInstanceWrapper getTomcatInstance() {
        return tomcatInstance
    }

    void setTomcatInstance(TomcatInstanceWrapper tomcatInstance) {
        this.tomcatInstance = tomcatInstance
    }

    @Override
    APIResponse startAndReturnAsyncTask() {
        APIResponse apiResponse = GlobalController.call(CommonMainStartServerLogicFunc.class, new APIRequest([
                ref: this
        ]))
        return apiResponse
    }

    def removeLog(CommonMissile ref) {
        def doConfigTableName = ref.getConfigTableName()
        def crtConfigObj = ref.getCrtDataObj()
        String ID = crtConfigObj['ID'] as String;
        String msgSource = (doConfigTableName.toLowerCase().contains("static") ? InfraKeys.MSG_SOURCE_STATIC_SERVER : InfraKeys.MSG_SOURCE_PROXY_SERVER) + ID
        InfraKeys.deleteLog(msgSource)
    }

    def doLog(CommonMissile ref, int logType, String msg) {
        def doConfigTableName = ref.getConfigTableName()
        def crtConfigObj = ref.getCrtDataObj()
        String ID = crtConfigObj['ID'] as String;
        InfraLog infraLog = new InfraLog(logType,
                (doConfigTableName.toLowerCase().contains("static") ? InfraKeys.MSG_SOURCE_STATIC_SERVER : InfraKeys.MSG_SOURCE_PROXY_SERVER) + ID,
                msg)
        InfraKeys.log(infraLog)
    }

    @Override
    boolean stopMissile() {
        doLog(this, InfraKeys.LOG_TYPE_PRIMARY_0, "Closing the server and related resources...")
        if (tomcatInstance != null) {
            tomcatInstance.shutdown()
        }
        doLog(this, InfraKeys.LOG_TYPE_PRIMARY_0, "Shut down.")
        return false
    }


}