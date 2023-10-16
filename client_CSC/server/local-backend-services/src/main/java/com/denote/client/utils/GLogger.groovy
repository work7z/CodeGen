package com.denote.client.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory


public class GLogger {
    private static Logger logger = LoggerFactory.getLogger("default");

    public static Logger g(){
        return logger;
    }

    public static Logger g(Class clz){
        return LoggerFactory.getLogger(clz);
    }

    public static Logger g(String clz){
        return LoggerFactory.getLogger(clz);
    }

    static void main(String[] args) {
        GLogger.g().debug("ok")
    }
    /**
     1, proxy server, including HTTP/HTTPS, etc..
     2, code generator, for supporting MyBatis, Spring
     3, Database brief connect
     */
}
