package com.denote.client.dto.dblink.impl

import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.DBLinkTypeInterface

class DB2_LinkType extends AbstractDBLinkType {
    @Override
    String getDriverClassName() {
        return 'com.ibm.db2.jcc.DB2Driver';
    }

    @Override
    String getProtocolType() {
        return 'jdbc:db2'
    }
}
