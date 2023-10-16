package com.denote.client.core

import com.denote.client.dto.connection.ConnectionManager
import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.connection.ConnectionLiveContext
import com.denote.client.dto.MavenCalcVM
import com.denote.client.dto.dblink.sql.SQLDefinition
import com.denote.client.exceptions.CannotConnectionException
import com.denote.client.exceptions.CannotExecuteException
import com.denote.client.exceptions.SolutionNotFoundException
import com.denote.client.handler.GlobalController
import com.denote.client.utils.GData
import com.denote.client.utils.GHttpUtils
import com.denote.client.utils.GLogger
import com.denote.client.utils.GSysConfigUtils
import com.denote.client.utils.GUtils

class DbLinkLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def g = GData.g();

        // TEMPLATE QUERY
        def preResultForDriver = GData.runTemplateUpdate(apiRequest, "driver-", "g_dblink_driver");
        if (preResultForDriver.resOk()) {
            return preResultForDriver;
        }
        def preResultForDbType = GData.runTemplateUpdate(apiRequest, "dbtype-", "g_dblink_dbtype");
        if (preResultForDbType.resOk()) {
            return preResultForDbType;
        }
        def p1 = GData.runTemplateUpdate(apiRequest, "opt-folder-", "g_dblink_connections_folder");
        if (p1.resOk()) {
            return p1;
        }
        def p2 = GData.runTemplateUpdate(apiRequest, "opt-conn-", "g_dblink_connections");
        if (p2.resOk()) {
            return p2;
        }
        def p3 = GData.runTemplateUpdate(apiRequest, "opt-scripts-", "g_dblink_connections_scripts");
        if (p3.resOk()) {
            return p3;
        }

        if (apiRequest.getActionType().equalsIgnoreCase("download-driver")) {
            if (apiRequest.param['DRIVER_ID'] == null) {
                return APIResponse.err("driver id is empty")
            }
            // clean all
            def downloadType = "db-driver"
            GHttpUtils.closeAllByType(downloadType)
            // action
            String uuid = GUtils.uuid();
            // init, started, error, verify, done
            def crtDownloadStatus = [status     : 'init',
                                     totalSize  : -1,
                                     currentSize: -1,];
            def commonClean = {
                InfraKeys.DOWNLOAD_STATUS_MAP.remove(uuid)
            }
            InfraKeys.DOWNLOAD_STATUS_MAP.put(uuid, crtDownloadStatus)
            def driObj = GData.g().queryFirst("select * from G_DBLINK_DRIVER WHERE ID=:DRIVER_ID",
                    [DRIVER_ID: apiRequest.param['DRIVER_ID']])
            if (true) {
                def groupId = driObj['MVN_GROUP_ID'] as String;
                def artifactId = driObj['MVN_ARTIFACT_ID'] as String;
                def version = driObj['MVN_VERSION'] as String
                MavenCalcVM mavenCalcVM = new MavenCalcVM(version, groupId, artifactId);
                mavenCalcVM.setMybaseurl(GSysConfigUtils.getBaseMavenLink())
                def jarURL = mavenCalcVM.getJarURL();
                def jarAscURL = mavenCalcVM.getJarAscURL()
                File saveFolder = new File(GUtils.getDriversDir(), "${groupId}/${artifactId}");
                return APIResponse.ok([folder    : saveFolder.getAbsolutePath(),
                                       base      : GSysConfigUtils.getBaseMavenLink(),
                                       groupId   : groupId,
                                       artifactId: artifactId,
                                       version   : version,
                                       meta      : driObj])
            }
        }

        if (apiRequest.getActionType() == 'driver-custom-list') {
            return APIResponse.ok(GData.g().query("""
select * from g_dblink_driver where dbtype_id=:val order by mvn_version desc
""", [val: apiRequest.param['DBTYPE_ID']]))
        }

        // SELF DEFINITION
        def sql_BasicQueryFolder = """
select * from g_dblink_connections_folder
"""
        def sql_BasicQueryConn = """
select 
 a.*,
 ${GData.g().listColumn("g_dblink_driver", "dbtype_id")}
from g_dblink_connections a 
left join  g_dblink_driver b on a.driver_id = b.id
left join g_dblink_dbtype c on a.dbtype_id = c.id
"""
        def getActivityByID = { int ID ->
            def crtConn = g.queryFirst("SELECT * FROM g_dblink_connections WHERE ID=:VAL", [VAL: ID])
            def getConnRes = GlobalController.call(DbLinkGetConnectionLogicFunc.class, new APIRequest([conn: crtConn,
                                                                                                       test: false]))
            ConnectionLiveContext activity = getConnRes.getContent() as ConnectionLiveContext
            return activity;
        }
        def getActivityByParamID = {
            return getActivityByID(apiRequest.param['ID'] as int)
        }
        def doConnection = { int ID ->
            def activity = getActivityByID(ID);
            try {
                activity.testConnection();
            } catch (Throwable err) {
                GLogger.g().error("got a failure of the test connection", err)
                String errStr = GUtils.getErrToStr(err)
                throw new CannotConnectionException(errStr)
            }
            return activity;
        }
        def disConnection = { int ID ->
            def crtConn = g.queryFirst("SELECT * FROM g_dblink_connections WHERE ID=:VAL", [VAL: ID])
            if (crtConn != null) {
                ConnectionManager.destroyLiveObject(Objects.toString(ID));
            }
        }
        def getCrtConnByParamId = {
            def conn = g.queryFirst("select * from g_dblink_connections where id=:ID", [ID: apiRequest.param['ID'] as int])
            return conn;
        }
        def getActivityAndPreparedAll = {
            def conn = getCrtConnByParamId();
            if (conn['IS_CONNECTION'] != 1) {
                GlobalController.call(DbLinkLogicFunc.class, new APIRequest(apiRequest.getActionCategory(), "conn-connect", [ID: conn['ID']]))
                conn = getCrtConnByParamId();
            }
            def queryObj = getActivityByParamID(apiRequest.param['ID'] as int)
            return queryObj
        }
        // api_path
        switch (apiRequest.getActionType()) {
            case 'scripts-list':
                def scriptsList = g.query("""
    select * from g_dblink_connections_scripts
""")
                scriptsList.each {
                    def conn_id = it['CONN_ID']
                    if (conn_id != null && conn_id != 0) {
                        def conn_data = g.queryFirst("select * from (${sql_BasicQueryConn}) where id=${conn_id}", [:]);
                        it['CONN_DATA'] = conn_data
                    }
                    def ACTIVITY_ID = it['ACTIVITY_ID']
                    if (ACTIVITY_ID != null && ACTIVITY_ID != 0) {
                        def activity = ConnectionManager.getLiveObject(ACTIVITY_ID as int)
                    }
                }
                return APIResponse.ok()
                break;
            case 'conn-list':
                Map<String, Object> rootReturn = [root: 1]
                def rootFolderList = g.query("""
select * from (${sql_BasicQueryFolder}) a where ifnull(parent_folder_id,0)=0
""", [:])
                handleFolderList(rootFolderList, sql_BasicQueryFolder, sql_BasicQueryConn)
                rootReturn['EXTRA_DATA_SUB_FOLDER'] = rootFolderList
                rootReturn['EXTRA_DATA_CONN'] = g.query("""
select * from (${sql_BasicQueryConn}) a where ifnull(folder_id,0) = 0
""")
                return APIResponse.ok(rootReturn)
                break;
            case 'conn-info':
                def connID = apiRequest.param['ID']
                def queryObj = g.queryFirst("""
SELECT * FROM (${sql_BasicQueryConn}) a where id=:ID
""", [ID: connID])
                if (queryObj == null) {
                    return APIResponse.err("No such a connection item")
                }
                return APIResponse.ok(queryObj)
                break;
            case 'conn-save':
                synchronized ("conn-save-dblink".toString().intern()) {
                    def updateVersion = GUtils.uuid().replaceAll("-", "")
                    def connData = apiRequest.param['CONN_DATA']
                    List<Map> listmap = [connData] as List<Map>;
                    saveFolderAndConnData(g, listmap, updateVersion, null);
                    // delete not-updated connection
                    // TODO: here need to clean old unuse connection
                    def noMoreUsedArr = g.query("SELECT * FROM g_dblink_connections where update_version != :VAL", [VAL: updateVersion])
                    noMoreUsedArr.each {
                        disConnection(it['ID'] as int)
                    }
                    g.exec("DELETE FROM g_dblink_connections_folder WHERE UPDATE_VERSION != :VAL", [VAL: updateVersion])
                    g.exec("DELETE FROM g_dblink_connections WHERE UPDATE_VERSION != :VAL", [VAL: updateVersion])
                }
                return APIResponse.ok()
                break;
            case 'conn-connect':
                doConnection(apiRequest.param['ID'] as int)
                g.exec("update g_dblink_connections set IS_CONNECTION = 1 where ID=:ID", [ID: apiRequest.param['ID'] as int])
                return APIResponse.ok()
                break;
            case 'conn-disconnect':
                disConnection(apiRequest.param['ID'] as int)
                g.exec("update g_dblink_connections set IS_CONNECTION = 0 where ID=:ID", [ID: apiRequest.param['ID'] as int])
                return APIResponse.ok()
                break;
            case 'conn-user-query':
                def activity = getActivityAndPreparedAll();
                def sql = apiRequest.param['SQL'] as String
                def param = apiRequest.param['PARAM'] as Map<String, Object>
                def editorId = apiRequest.param['EDITOR_ID'] as int;
                def actId = apiRequest.param['ACTIVITY_ID'] as int;
                def result = activity.queryByEditorIdAndActId(editorId, actId, sql as String, param);
                return APIResponse.ok(result)
                break;
            case 'conn-system-query':
                SQLDefinition sqlDefinition = null;
                def activity = getActivityAndPreparedAll();
                def dblinktype = activity.getDbLinkType();
                def type = apiRequest.param['TYPE'] as String
                switch (type) {
                    case 'show-databases':
                        sqlDefinition = dblinktype.getShowDatabases(apiRequest.param['IPT'] as String, [:])
                        break;
                    case 'show-tables':
                        sqlDefinition = dblinktype.getShowTables(apiRequest.param['IPT'] as String, [:])
                        break;
                    default:
                        throw new CannotExecuteException("No such a type ${type}")
                }
                def connID = apiRequest.param['CONN_ID'] as int
                def sql = sqlDefinition.getSQL()
                def param = sqlDefinition.getParams() as Map<String, Object>
                def result = activity.queryDirect(connID, sql, param);
                if (sqlDefinition.getHandleLogic() != null) {
                    sqlDefinition.getHandleLogic().apply(result)
                }
                return APIResponse.ok(result)
                break;
            case 'conn-user-exec':
                def activity = getActivityAndPreparedAll();
                def sql = apiRequest.param['SQL'] as String
                def param = apiRequest.param['PARAM'] as Map<String, Object>
                def editorId = apiRequest.param['EDITOR_ID'] as int;
                def actId = apiRequest.param['ACTIVITY_ID'] as int;
                def result = activity.execByEditorIdAndActId(editorId, actId, sql as String, param);
                return APIResponse.ok(result)
                break;
            case 'conn-user-call':
                def activity = getActivityAndPreparedAll();
                def sql = apiRequest.param['SQL'] as String
                def param = apiRequest.param['PARAM'] as Map<String, Object>
                def editorId = apiRequest.param['EDITOR_ID'] as int;
                def actId = apiRequest.param['ACTIVITY_ID'] as int;
                def result = activity.callByEditorIdAndActId(editorId, actId, sql as String, param);
                return APIResponse.ok(result)
                break;
            case 'conn-test':
                def tmp_crtConn = apiRequest.param['CONN'];
                if (tmp_crtConn == null) {
                    tmp_crtConn = g.queryFirst("select * from g_dblink_connections where id=:ID", [ID: apiRequest.param['ID'] as int]);
                }
                def getConnRes = GlobalController.call(DbLinkGetConnectionLogicFunc.class,
                        new APIRequest([conn: tmp_crtConn, test: true]))
                ConnectionLiveContext activity = getConnRes.getContent() as ConnectionLiveContext
                try {
                    activity.testConnection();
                    return APIResponse.ok()
                } catch (Throwable err) {
                    GLogger.g().error("got a failure of the test connection", err)
                    String errStr = GUtils.getErrToStr(err)
                    return APIResponse.err(errStr)
                }
                return APIResponse.ok()
                break;
        }
        throw new SolutionNotFoundException()
    }
    def handleFolderList = { List myarr, String sql_BasicQueryFolder, String sql_BasicQueryConn ->
        def g = GData.g();
        myarr.each {
            def childrenFolderId = it['CHILDREN_FOLDER_ID'] as String
            if (childrenFolderId && !childrenFolderId.isEmpty()) {
                def subFolderList = g.query("""
select * from (${sql_BasicQueryFolder}) a where id in (${childrenFolderId})
""")
                it['EXTRA_DATA_SUB_FOLDER'] = subFolderList;
                handleFolderList(subFolderList, sql_BasicQueryFolder, sql_BasicQueryConn)
            }

            it['EXTRA_DATA_CONN'] = g.query("""
select * from (${sql_BasicQueryConn}) a where ifnull(folder_id,0) = ${it['ID']}
""")
        }
    }

    static void saveFolderAndConnData(GData gData, List<Map> arr, String updateVersion, Integer parentFolderId) {
        arr.each { def crtFolderItem ->
            def saveFolder = { Map folderMap ->
                folderMap['UPDATE_VERSION'] = updateVersion
                folderMap['PARENT_FOLDER_ID'] = crtFolderItem['ID']
                GData.modify("g_dblink_connections_folder", "upset", folderMap, true)
            }
            def saveConn = { Map connMap ->
                connMap['UPDATE_VERSION'] = updateVersion
                connMap['FOLDER_ID'] = crtFolderItem['ID']
                GData.modify("g_dblink_connections",
                        "upset",
                        connMap,
                        true)
            }
            if (crtFolderItem['root'] != 1) {
                // the field crtFolderItem is a folder
                saveFolder(crtFolderItem)
            }
            def EXTRA_DATA_SUB_FOLDER = crtFolderItem['EXTRA_DATA_SUB_FOLDER'] as List<Map>
            if (EXTRA_DATA_SUB_FOLDER != null && EXTRA_DATA_SUB_FOLDER.size() != 0) {
                saveFolderAndConnData(gData, EXTRA_DATA_SUB_FOLDER, updateVersion, crtFolderItem['ID'] as Integer)
            }

            if (crtFolderItem['ID'] != null && EXTRA_DATA_SUB_FOLDER != null) {
                def childIDLists = EXTRA_DATA_SUB_FOLDER.collect({ it -> it['ID'] }).join(",")
                GData.g().exec("""
update g_dblink_connections_folder set children_folder_id = :CHILD_ID_LIST where ID=:ID
""", [
                        ID           : crtFolderItem['ID'],
                        CHILD_ID_LIST: childIDLists
                ])
                println "Got the ID lists: ${childIDLists}"
            }

            // update
            def subFolderIdArr = []
            EXTRA_DATA_SUB_FOLDER.each {
                subFolderIdArr.add(it['ID'])
            }
            if (subFolderIdArr == null) {
                subFolderIdArr = [];
            }
            GData.g().exec("""
update g_dblink_connections_folder set children_folder_id = :CHILD_FOLDER_ID,parent_folder_id=:PID where ID = :FOLDER_ID
""", [FOLDER_ID      : crtFolderItem['ID'],
      PID            : parentFolderId,
      CHILD_FOLDER_ID: subFolderIdArr.join(","),
            ])

            def EXTRA_DATA_CONN = crtFolderItem['EXTRA_DATA_CONN'] as List<Map>
            if (EXTRA_DATA_CONN != null) {
                EXTRA_DATA_CONN.each { Map it -> saveConn(it)
                }
            }
        }
    }
}

//            GUtils.highrun({
//                GlobalController.call(MavenDownloadJarLogicFunc.class, new APIRequest(
//                        [
//                                downloadType: downloadType,
//                                uuid        : uuid,
//                                groupId     : driObj['MVN_GROUP_ID'],
//                                artifactId  : driObj['MVN_ARTIFACT_ID'],
//                                version     : driObj['MVN_VERSION'],
//                                onProgress  : { int currentSize, int totalSize, String status, String errMsg ->
//                                    crtDownloadStatus['totalSize'] = totalSize;
//                                    crtDownloadStatus['currentSize'] = currentSize;
//                                    crtDownloadStatus['status'] = status;
//                                    if (errMsg) {
//                                        crtDownloadStatus['errMsg'] = errMsg
//                                        GUtils.highrun({
//                                            Thread.sleep(TimeUnit.MINUTES.toMillis(1))
//                                            commonClean()
//                                        })
//                                        return;
//                                    }
//                                    if (status == 'done') {
//                                        GUtils.highrun({
//                                            Thread.sleep(TimeUnit.MINUTES.toMillis(1))
//                                            commonClean()
//                                        })
//                                        return;
//                                    }
//                                }]))
//            })
//            return APIResponse.ok(uuid);
