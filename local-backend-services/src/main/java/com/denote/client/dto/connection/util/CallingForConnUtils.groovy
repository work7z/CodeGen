package com.denote.client.dto.connection.util

import com.denote.client.core.ConnActivityDetailLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.connection.result.CallResult
import com.denote.client.dto.connection.result.ExecuteResult
import com.denote.client.dto.connection.result.QueryResult
import com.denote.client.handler.GlobalController

import java.sql.Connection

class CallingForConnUtils {
    public static QueryResult query(Connection conn, int connID, String SQL, Map<String, Object> param) {
        APIResponse res = internalRun(conn, 'query', connID, SQL, param)
        return res.getContent() as QueryResult;
    }

    public static ExecuteResult exec(Connection conn, int connID, String SQL, Map<String, Object> param) {
        APIResponse res = internalRun(conn, 'exec', connID, SQL, param)
        return res.getContent() as ExecuteResult;
    }

    public static CallResult call(Connection conn, int connID, String SQL, Map<String, Object> param) {
        APIResponse res = internalRun(conn, 'call', connID, SQL, param)
        return res.getContent() as CallResult;
    }

    private static APIResponse internalRun(Connection conn, String type, int connID, String SQL, Map<String, Object> param) {
        if (param == null) {
            param = [:]
        }
        def res = GlobalController.call(ConnActivityDetailLogicFunc.class, new APIRequest([type     : type,
                                                                                           conn     : conn,
                                                                                           arg_SQL  : SQL,
                                                                                           arg_param: param,
                                                                                           arg_connID   : connID]))
        return res
    }


}
