package com.denote.client.concurrent

import com.denote.client.dto.APIResponse
import com.denote.client.handler.extra.CommonMissile

class ToughMissile {
    Thread executedThread = null;
    MissileRunnable executeBusinessRunnable = null;
    long startUpTimestamp;
    String groupKey;
    String serverKey;
    APIResponse sysRes;

    def kill() {
        if (executeBusinessRunnable instanceof CommonMissile) {
            CommonMissile serverExecutorThread = executeBusinessRunnable as CommonMissile;
            serverExecutorThread.stopMissile();
        }
        MissionDispatchCentre.destroyBaseAndThread(groupKey, serverKey)
    }
}
