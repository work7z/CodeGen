package com.denote.client.dto.dblink.impl

import com.denote.client.dto.dblink.AbstractDBLinkType
import com.denote.client.dto.dblink.DBLinkTypeInterface

class Oracle_LinkType extends AbstractDBLinkType {
    @Override
    String getDriverClassName() {
        return 'oracle.jdbc.driver.OracleDriver';
    }

    @Override
    String getProtocolType() {
        return 'jdbc:oracle'
    }
}
