package com.denote.client.core

import com.alibaba.druid.pool.DruidPooledConnection
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.connection.ConnectionLiveContext
import com.denote.client.dto.connection.result.CallResult
import com.denote.client.dto.connection.result.CommonDBResult
import com.denote.client.dto.connection.result.ExecuteResult
import com.denote.client.dto.connection.result.QueryResult
import com.denote.client.exceptions.CannotExecuteException
import com.denote.client.handler.GlobalController
import com.denote.client.utils.Countable
import com.denote.client.utils.GData
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.support.JdbcUtils

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement

class ConnActivityDetailLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def param = apiRequest.param;
        def conn_inst = param['conn'] as DruidPooledConnection
        def type = param['type']
        def arg_SQL = param['arg_SQL'] as String
        def arg_param = param['arg_param'] as Map<String, Object>
        def arg_ID = param['arg_connID'] as int
        // get connection
        def g = GData.g();
        def conn = g.queryFirst("select * from g_dblink_connections where id=:ID", [ID: arg_ID])
        if (conn.IS_CONNECTION != 1) {
            GlobalController.call(DbLinkLogicFunc.class, new APIRequest("dblink", "conn-connect", [ID: arg_ID]))
            conn = g.queryFirst("select * from g_dblink_connections where id=:ID", [ID: arg_ID])
        }
        CommonDBResult commonDBResult = null;
        switch (type) {
            case 'query':
                def result = new QueryResult()
                def prepareStatement = conn_inst.prepareStatement(arg_SQL)
                Countable _ct1 = new Countable()
                def rs = prepareStatement.executeQuery()
                result.setOptTimeMiles(_ct1.countMilesRawValue())
                try {
                    List<Map> finalQueryResultList = new ArrayList<>()
                    List<String> columnIndexArr = []
                    while (rs.next()) {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        def crtRow = [:]
                        for (int i = 1; i <= columnCount; i++) {
                            def crtKey = (JdbcUtils.lookupColumnName(rsmd, i))
                            if (!columnIndexArr.contains(crtKey)) {
                                columnIndexArr.push(crtKey)
                            }
                            def crtValue = rs.getObject(i)
                            crtRow[crtKey] = [value: crtValue,
                                              clz  : crtValue == null ? 'null' : crtValue.getClass().getCanonicalName()]
                        }
                        finalQueryResultList.add(crtRow)
                    }
                    result.setDataList(finalQueryResultList)
                    result.setColumnIndexArr(columnIndexArr)
                    commonDBResult = result
                } finally {
                    rs.close()
                }
                break;
            case 'exec':
                def result = new ExecuteResult();
                def prepareStatement = conn_inst.prepareStatement(arg_SQL, Statement.RETURN_GENERATED_KEYS)
                Countable countable = new Countable()
                def updateRows = prepareStatement.executeUpdate()
                result.setOptTimeMiles(countable.countMilesRawValue())
                result.setUpdateRows(updateRows)
                commonDBResult = result
                // get generated keys
                ResultSet keys = prepareStatement.getGeneratedKeys()
                try {
                    def fetchSize = keys.getFetchSize()
                    def arr = [];
                    while (keys.next()) {
                        ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper();
                        def the_row = columnMapRowMapper.mapRow(keys, keys.getRow())
                        arr.push(the_row);
                    }
                    result.setGeneratedKeys(arr);
                } catch (Throwable err) {
                    throw err
                } finally {
                    keys.close()
                }
                break;
            case 'call':
                commonDBResult = new CallResult()
                break;
        }
        if (commonDBResult == null) {
            throw new CannotExecuteException("No such execute type ${type}")
        }
        return APIResponse.ok(commonDBResult)
    }
}
