package com.denote.client.dto.dblink.sql

import com.denote.client.dto.connection.ConnectionLiveContext
import com.denote.client.dto.connection.result.QueryResult

import java.util.function.Function

class SQLDefinition {
    String SQL;
    Map<String, Object> params;
    Function<QueryResult, Void> handleLogic;

    SQLDefinition(String SQL, Map<String, Object> params, Function<QueryResult, Void> handleLogic) {
        this.SQL = SQL
        this.params = params
        this.handleLogic = handleLogic
    }
}
