package com.denote.client.concurrent

class WebHolder {
    public static final String KEY_CRT_DATA_OBJ = "KEY_CRT_DATA_OBJ";
    public static final String KEY_API_PARAM = "API_PARAM";
    public static final String KEY_COMMON_MISSILE_OBJ = "KEY_COMMON_MISSILE_OBJ";

    public static ThreadLocal<Map> SERVLET_CONTEXT_HOLDER = new ThreadLocal<Map>() {
        @Override
        protected Map initialValue() {
            return new HashMap<>()
        }
    }

//    public static def get(String key) {
//        return SERVLET_CONTEXT_HOLDER.get().get(key)
//    }

    public static def set(String key, Object value) {
        SERVLET_CONTEXT_HOLDER.get().put(key, value)
    }
}
