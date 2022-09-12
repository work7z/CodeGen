package com.denote.client.concurrent

import java.util.concurrent.ConcurrentHashMap

class MissionDispatchCentre {
    public static final String GROUP_KEY_STATIC_SERVER = "st_";
    public static final String GROUP_KEY_PROXY_SERVER = "px_";
    private static Map<String, ToughMissile> globalDispatchMap = new ConcurrentHashMap<>();

    public static ToughMissile launch(String groupKey, String serverKey, MissileRunnable runnable) {
        // set priority to high, and then execute it
        def sysRes = runnable.startAndReturnAsyncTask()
        if (!sysRes.resOk()) {
            def missile = new ToughMissile()
            missile.setSysRes(sysRes)
            return missile
        }
        Thread prepareThread = sysRes.content as Thread
        if (prepareThread == null) {
            return null;
        }
        prepareThread.setPriority(Thread.MAX_PRIORITY)
        prepareThread.start()
        // save into globalDispatchMap
        ToughMissile toughMissile = new ToughMissile();
        toughMissile.setGroupKey(groupKey)
        toughMissile.setServerKey(serverKey)
        toughMissile.setExecutedThread(prepareThread)
        toughMissile.setExecuteBusinessRunnable(runnable)
        toughMissile.setStartUpTimestamp(System.currentTimeMillis())
        toughMissile.setSysRes(sysRes)
        globalDispatchMap.put(getEntireKey(groupKey, serverKey), toughMissile)
        return toughMissile
    }

    public static ToughMissile getAndRemoveFromMap(String groupKey, String serverKey) {
        def obj = get(groupKey, serverKey);
        globalDispatchMap.remove(getEntireKey(groupKey, serverKey))
        return obj;
    }

    public static ToughMissile get(String groupKey, String serverKey) {
        return globalDispatchMap.get(getEntireKey(groupKey, serverKey))
    }

    public static ToughMissile destroyBaseAndThread(String groupKey, String serverKey) {
        def missile = get(groupKey, serverKey)
        if (missile == null) {
            return null;
        }
        def thread = missile.getExecutedThread()
        // direct stop since it's a small application
        thread.stop();
        return missile
    }

    private static String getEntireKey(String groupKey, String serverKey) {
        return groupKey + serverKey
    }
}
