package com.denote.client.dto

class InfraLog {
    int logType
    String msgSource
    String msgContent

    InfraLog(int logType, String msgSource, String msgContent) {
        this.logType = logType
        this.msgSource = msgSource
        this.msgContent = msgContent
    }

}
