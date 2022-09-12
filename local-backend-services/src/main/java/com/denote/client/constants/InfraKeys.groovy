package com.denote.client.constants

import com.denote.client.dto.InfraLog
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils
import org.apache.commons.lang3.RandomUtils

import java.security.KeyStore
import java.util.concurrent.CountDownLatch

class InfraKeys {
    public static final CountDownLatch WAIT_SYSTEM_INIT = new CountDownLatch(1);
    public static final int LOG_TYPE_PRIMARY_0 = 0
    public static final int LOG_TYPE_SUCCESS_1 = 1
    public static final int LOG_TYPE_DANGER_2 = 2
    public static final int LOG_TYPE_WARNING_3 = 3
    public static final int LOG_TYPE_DEBUG_4 = 4
    public static final int LOG_TYPE_DANGER_DEBUG_4 = 14
    public static final int LOG_TYPE_INTERNAL_5 = 5
    public static final String INFRA_P_UID = UUID.randomUUID().toString().replaceAll("-", "")
    public static final String VERSION = "0.0.1-RC"
    public static Integer BOOT_RECORD_ID = null
    public static Map<String, Map<String, Object>> DOWNLOAD_STATUS_MAP = [:]

    public static String CRT_LIVE_ID = null;
    public static String CRT_DOWNLOAD_ID = GUtils.uuid()

    public static final Map<String, CountDownLatch> WAIT_COUNT_LATCH_FOR_NEW_LOGS = new HashMap<>();

    public static final String MSG_SOURCE_STATIC_SERVER = "st.server."
    public static final String MSG_SOURCE_PROXY_SERVER = "px.server."
    public static final String MSG_SOURCE_SYSTEM = "system"

    public static String noMore(String value, Integer len) {
        if (value == null || value.length() < len) {
            return value
        } else {
            return value.substring(0, len)
        }
    }

    public static void log(InfraLog infraLog) {
        String msgSource = infraLog.getMsgSource()
        def map = [
                LOG_TYPE   : infraLog.getLogType(),
                MSG_SOURCE : msgSource,
                MSG_CONTENT: infraLog.getMsgContent(),
                THREAD_ID  : noMore(Thread.currentThread().getName(), 90),
                THREAD_NAME: noMore(Thread.currentThread().getId() + "", 90),
                PUID       : INFRA_P_UID
        ]
        GData.modify("g_infra_logging", "insert", map)
        triggerWakeMsgSource(msgSource)
    }

    private static void triggerWakeMsgSource(String msgSource) {
        def lockKey = WAIT_COUNT_LATCH_FOR_NEW_LOGS.get(msgSource)
        if (lockKey != null) {
            lockKey.countDown()
        }
    }

    public static void updateLatestActiveTime() {
        if (BOOT_RECORD_ID == null) {
            return;
        }
        GData.g().exec("update g_infra_boot_record set last_active_time=now() where id=:ID", [ID: BOOT_RECORD_ID])
    }

    public static int UPDATE_COUNT = 0

    public static void updateLatestActiveTimeWithDelay() {
        if (UPDATE_COUNT > 15) {
            updateLatestActiveTime()
            UPDATE_COUNT = 0
        }
        UPDATE_COUNT++
    }

    static void deleteLog(String msgSource) {
        GData.g().exec("delete from g_infra_logging where msg_source=:VAL", [VAL: msgSource])
        triggerWakeMsgSource(msgSource)
    }

    static void main(String[] args) {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        char[] password = "some password".toCharArray();
        ks.load(null, password);

// Store away the keystore.
        FileOutputStream fos = new FileOutputStream(new File(GUtils.get_USER_HOME_DIR(), "newKeyStoreFileName"));
        ks.store(fos, password);
        fos.close();
    }
}

