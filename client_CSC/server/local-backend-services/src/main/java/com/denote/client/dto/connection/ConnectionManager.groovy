package com.denote.client.dto.connection

import java.util.concurrent.atomic.AtomicInteger

class ConnectionManager {
    private static Map<String, ConnectionLiveContext> LIVE_CONTEXT = new HashMap<>()
    public static Map<Integer, ConnectionMemContext> MEM_CONTEXT = new HashMap<>()
    public static AtomicInteger ACTIVITY_ASSIGNEE = new AtomicInteger(10000);

    public static void setLiveObject(Integer id, ConnectionLiveContext inst) {
        if (id == null) {
            throw new IllegalArgumentException("Placing the field value ${id} with ${inst} is incorrect")
        }
        setLiveObject(Objects.toString(id), inst);
    }

    public static ConnectionLiveContext getLiveObject(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot find by an empty value ${id}")
        }
        return getLiveObject(Objects.toString(id));
    }

    public static void setLiveObject(String id, ConnectionLiveContext inst) {
        def prev = LIVE_CONTEXT.get(id);
        if (prev != null) {
            prev.destroy()
        }
        LIVE_CONTEXT.put(id, inst);
    }

    public static ConnectionLiveContext getLiveObject(String id) {
        return LIVE_CONTEXT.get(id);
    }

    static void destroyLiveObject(String id) {
        def obj = getLiveObject(id)
        if (obj != null) {
            obj.destroy()
        }
        LIVE_CONTEXT.remove(id)
    }
}
