package com.denote.client.concurrent

import com.denote.client.config.proxy.ProxyMainLogicServlet

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class GenerateAssignCentre {
    public static Map<String, AtomicInteger> TABLE_UNIQUE_ASSIGN = new ConcurrentHashMap<>()
    // TODO: here the field need to be vacuumed duly
    public static Map<Integer, ProxyMainLogicServlet> PROXY_SERVLET_MAPPING_BY_RULE_ID = new HashMap<>();
}
