package com.denote.client.dto.connection

import ch.qos.logback.core.util.CloseUtil
import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.druid.pool.DruidPooledConnection
import com.denote.client.core.ConnActivityDetailLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.connection.result.CallResult
import com.denote.client.dto.connection.result.ExecuteResult
import com.denote.client.dto.connection.result.QueryResult
import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.DBTypeRef
import com.denote.client.exceptions.CannotExecuteException
import com.denote.client.handler.GlobalController

import javax.management.Query
import java.sql.Connection
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger

class ConnectionLiveContext {
    boolean test = false;
    DruidDataSource dataSource;
    Map<String, Object> crtConn;
    Map<String, Object> crtDriver;
    int crtConnID;
    AbstractDBLinkType dbLinkType;
    private List<ConnectionActivityWithEditor> crtEditorsWithActivityList = [];

    ConnectionLiveContext(int crtConnID, String dblinkType, boolean test, DruidDataSource dataSource) {
        if (test) {
            dataSource.setMaxActive(1)
            dataSource.setMinIdle(1)
            dataSource.setMaxCreateTaskCount(1)
        }
        this.crtConnID = crtConnID;
        this.test = test
        this.dataSource = dataSource
        this.dbLinkType = DBTypeRef.ALL_DB_TYPE[dblinkType]
        def editor = new ConnectionActivityWithEditor(
                crtConnID,
                ConnectionManager.ACTIVITY_ASSIGNEE.incrementAndGet(),
                0,
                this
        )
        editor.setInternal(true)
        this.crtEditorsWithActivityList.push(editor)
    }

    public void destroy() {
        CloseUtil.closeQuietly(this.dataSource)
    }

    DruidPooledConnection getConnection() {
        return this.dataSource.getConnection(3000)
    }

    void testConnection() {
        def conn = this.getConnection()
        def statement = conn.prepareStatement("select 1");
        statement.execute()
    }

    ExecuteResult execByEditorIdAndActId(int editorId, int activityId, String sql, Map<String, Object> params) {
        def item = crtEditorsWithActivityList.find({ it.getEditorId() == editorId && it.getActivityId() == activityId })
        if (item == null) {
            throw new CannotExecuteException("No available editor and activity instance to be used")
        }
        return item.exec(sql, params);
    }

    CallResult callByEditorIdAndActId(int editorId, int activityId, String sql, Map<String, Object> params) {
        def item = crtEditorsWithActivityList.find({ it.getEditorId() == editorId && it.getActivityId() == activityId })
        if (item == null) {
            throw new CannotExecuteException("No available editor and activity instance to be used")
        }
        return item.call(sql, params);
    }

    QueryResult queryByEditorIdAndActId(int editorId, int activityId, String sql, Map<String, Object> params) {
        def item = crtEditorsWithActivityList.find({ it.getEditorId() == editorId && it.getActivityId() == activityId })
        if (item == null) {
            throw new CannotExecuteException("No available editor and activity instance to be used")
        }
        return item.query(sql, params);
    }

    QueryResult queryDirect(int connID, String sql, Map<String, Object> params) {
    }
}
