package com.denote.client.dto.connection

import com.denote.client.dto.connection.result.CallResult
import com.denote.client.dto.connection.result.ExecuteResult
import com.denote.client.dto.connection.result.QueryResult
import com.denote.client.dto.connection.util.CallingForConnUtils

import java.sql.Connection

class ConnectionActivityWithEditor {
    boolean internal = false;
    int connId;
    int editorId;
    int activityId;
    ConnectionLiveContext connectionLiveContext;
    List<EachConnectionHoldingWrap> connectionHoldingWraps = [];

    ConnectionActivityWithEditor(int connId, int editorId, int activityId, ConnectionLiveContext connectionLiveContext) {
        this.connId = connId;
        this.editorId = editorId
        this.activityId = activityId;
        this.connectionLiveContext = connectionLiveContext
    }


    QueryResult query(String sql, Map<String, Object> params) {
        Connection connection = this.getConnection();
        return CallingForConnUtils.query(connection, connId, sql, params)
    }

    ExecuteResult exec(String sql, Map<String, Object> params) {
        Connection connection = this.getConnection();
        return CallingForConnUtils.exec(connection, connId, sql, params)
    }

    CallResult call(String sql, Map<String, Object> params) {
        Connection connection = this.getConnection();
        return CallingForConnUtils.call(connection, connId, sql, params)
    }

    public Connection getConnection() {
        def item = connectionHoldingWraps.find({ !it.isUsing })
        if (item == null) {
            item = connectionLiveContext.getConnection()
            connectionHoldingWraps.push(new EachConnectionHoldingWrap(item))
        }
        return item;
    }


}
