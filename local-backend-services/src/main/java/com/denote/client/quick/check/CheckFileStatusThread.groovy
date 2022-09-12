package com.denote.client.quick.check

import com.denote.client.constants.InfraKeys
import com.denote.client.utils.GData
import com.denote.client.utils.GLogger
import com.denote.client.utils.GUtils

class CheckFileStatusThread implements Runnable {
    private File finalPUIDFile;

    CheckFileStatusThread(File finalPUIDFile) {
        this.finalPUIDFile = finalPUIDFile
        lastModified = finalPUIDFile.lastModified()
    }

    File getFinalPUIDFile() {
        return finalPUIDFile
    }

    void setFinalPUIDFile(File finalPUIDFile) {
        this.finalPUIDFile = finalPUIDFile
    }


    long lastModified = -1;

    @Override
    void run() {
        while (true) {
            checkOnce()
            sleep(2000)
        }
    }

    public void checkOnce() {
        if (lastModified != finalPUIDFile.lastModified()) {
            def puid = finalPUIDFile.readLines().join("")
            if (puid != null && puid.length() != 0 && puid != InfraKeys.INFRA_P_UID) {
                GLogger.g().info("Exited due to the process isn't the latest one")
                GData.closeConn()
                System.exit(-100)
            }
        }
    }

}
