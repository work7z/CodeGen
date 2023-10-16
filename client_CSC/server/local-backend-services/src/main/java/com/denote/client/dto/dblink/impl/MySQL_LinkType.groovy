package com.denote.client.dto.dblink.impl

import com.denote.client.dto.connection.ConnectionLiveContext
import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.sql.SQLDefinition

import java.util.function.Function

class MySQL_LinkType extends AbstractDBLinkType {
    @Override
    String getDriverClassName() {
        return 'com.mysql.jdbc.Driver';
    }

    @Override
    String getProtocolType() {
        return 'jdbc:mysql'
    }

    @Override
    SQLDefinition getShowDatabases(String input, Map<String, Object> params) {
        return new SQLDefinition("show databases like '%${filterTableName(input)}%'", params, new Function<QueryResult, Void>() {
            @Override
            Void apply(QueryResult queryResult) {
//                queryResult.getDataList()
                return null
            }
        })
    }

    @Override
    SQLDefinition getShowTables(String input, Map<String, Object> params) {
        return new SQLDefinition("show tables like '%${filterTableName(input)}%'", params, new Function<QueryResult, Void>() {
            @Override
            Void apply(QueryResult queryResult) {
//                queryResult.getDataList()
                return null
            }
        })
    }

    private static String filterTableName(String tables) {
        if (tables == null) {
            return '';
        }
        return tables.replaceAll("'", "")
    }
}
