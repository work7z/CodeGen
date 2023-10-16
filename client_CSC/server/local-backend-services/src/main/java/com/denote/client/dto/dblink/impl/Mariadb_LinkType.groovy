package com.denote.client.dto.dblink.impl

import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.DBLinkTypeInterface

class Mariadb_LinkType extends MySQL_LinkType {
    @Override
    String getDriverClassName() {
        return 'org.mariadb.jdbc.Driver';
    }

    @Override
    String getProtocolType() {
        return 'jdbc:mysql'
    }
}
