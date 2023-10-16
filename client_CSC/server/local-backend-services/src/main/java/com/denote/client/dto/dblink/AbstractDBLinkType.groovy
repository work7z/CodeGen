package com.denote.client.dto.dblink


import com.denote.client.dto.dblink.sql.SQLDefinition

abstract class AbstractDBLinkType implements DBLinkTypeInterface {
    public SQLDefinition getShowDatabases(String input, Map<String, Object> params) {
        // TODO: support all database;
        throw new UnsupportedOperationException()
    }

    public SQLDefinition getShowTables(String input, Map<String, Object> params) {
        // TODO: support all database;
        throw new UnsupportedOperationException()
    }

}
