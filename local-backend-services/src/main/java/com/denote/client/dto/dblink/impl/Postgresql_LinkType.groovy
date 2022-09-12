package com.denote.client.dto.dblink.impl

import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.DBLinkTypeInterface

class Postgresql_LinkType extends AbstractDBLinkType {
    @Override
    String getDriverClassName() {
        return 'org.postgresql.Driver';
    }

    @Override
    String getProtocolType() {
        return 'jdbc:postgresql'
    }
}
