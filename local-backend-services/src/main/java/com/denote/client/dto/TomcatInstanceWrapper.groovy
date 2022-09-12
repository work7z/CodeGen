package com.denote.client.dto

import com.denote.client.concurrent.WebHandleServletHolder
import com.denote.client.handler.extra.CommonMissile
import org.apache.catalina.LifecycleState
import org.apache.catalina.startup.Tomcat

class TomcatInstanceWrapper {
    Tomcat instance;

    CommonMissile ref;

    TomcatInstanceWrapper(Tomcat instance) {
        this.instance = instance
    }

    public TomcatInstanceWrapper start() {
        try {
            System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true")
            instance.start();
        } catch (Throwable throwable) {
            if (instance != null) {
                this.shutdown()
                instance.destroy()
            }
            throw throwable
        }
        return this;
    }


    public boolean isStartFailed() {
        return instance.getConnector().getState() != LifecycleState.STARTED;
    }

    public TomcatInstanceWrapper waitAfterStarted() {
        instance.getServer().await();
        return this;
    }

    public List<Throwable> getAllError() {
        return WebHandleServletHolder.TOMCAT_SERVER_BOOT_ERROR.get();
    }

    public void shutdown() {
        if (instance != null && instance.getConnector() != null && instance.getConnector().getState() == LifecycleState.STARTED) {
            instance.stop()
            instance.destroy()
        }
    }
}
