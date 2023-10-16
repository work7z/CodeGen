package com.denote.client.dto.dblink.impl

import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.DBLinkTypeInterface

class Sybase_LinkType extends AbstractDBLinkType {
    @Override
    String getDriverClassName() {
        return 'com.sybase.jdbc.SybDriver';
    }

    @Override
    String getProtocolType() {
        return 'jdbc:sybase'
    }
}
