package com.denote.client.concurrent;

import com.denote.client.handler.extra.CommonMissile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class WebHandleServletHolder {
    public static final ThreadLocal<ServletConfig> SYS_CONFIG = new ThreadLocal<>();
    public static final ThreadLocal<CommonMissile> SYS_REF = new ThreadLocal<>();
    public static final ThreadLocal<HttpServletRequest> SYS_REQ = new ThreadLocal<>();
    public static final ThreadLocal<HttpServletResponse> SYS_RES = new ThreadLocal<>();
    public static final ThreadLocal<ServletContext> SYS_CONTEXT = new ThreadLocal<>();
    public static final ThreadLocal<List<Throwable>> TOMCAT_SERVER_BOOT_ERROR = new ThreadLocal<List<Throwable>>() {
        @Override
        protected List<Throwable> initialValue() {
            return new ArrayList<>();
        }
    };
}
