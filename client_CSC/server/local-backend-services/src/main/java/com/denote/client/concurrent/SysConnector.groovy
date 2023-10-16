package com.denote.client.concurrent

import org.apache.catalina.LifecycleException
import org.apache.catalina.connector.Connector

class SysConnector extends Connector {

    SysConnector(String protocol) {
        super(protocol)
    }

    @Override
    protected void initInternal() throws LifecycleException {
        try {
            super.initInternal();
        } catch (Throwable e) {
            WebHandleServletHolder.TOMCAT_SERVER_BOOT_ERROR.get().add(e);
            throw e;
        }
    }


}
