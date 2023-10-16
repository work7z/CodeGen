package com.denote.client.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.RandomUtils

class GSysConfigUtils {
    public static Map<String, Object> getBasisRepositoryMap() {
        def obj = JSON.parseObject(GUtils.readClzFile("basis-repository.json"), Map.class)
        return obj;
    }

    public static String getBaseMavenLink() {
        def mirror = getMirror()
        def basis = getBasisRepositoryMap()
        def object = basis['core'][mirror] as List
        def myidx = RandomUtils.nextInt(0, object.size())
        return object[myidx]
    }

    public static Map<String, Object> getUserConfig() {
        String initJSONStr = GUtils.readFile(new File(GUtils.getAppDownloadDir(), "init.json"))
        return JSONObject.parseObject(initJSONStr, Map.class);
    }

    public static String getMirror() {
        def object = getUserConfig()
        return object['mirror']
    }
}
