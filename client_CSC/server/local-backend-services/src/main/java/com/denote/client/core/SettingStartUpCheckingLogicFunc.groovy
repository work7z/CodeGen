package com.denote.client.core

import com.denote.client.constants.SettingKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData

class SettingStartUpCheckingLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        // initializing settings
        [
                [
                        SettingKeys.LOGGING_VIEW_HEIGHT, "620"
                ],
                [
                        SettingKeys.LOGGING_VIEW_LINE_BREAK, "nowrap"
                ],
                [
                        SettingKeys.LOGGING_VIEW_RETRIEVE_LINES_NUMBER, 300
                ],
                [
                        SettingKeys.LOGGING_VIEW_SEARCH_LOG_TYPE, "@all"
                ],
                [
                        SettingKeys.LOGGING_VIEW_FONT_SIZE, "14"
                ],
                [
                        SettingKeys.LOGGING_VIEW_REALTIME_LOAD, "true"
                ]
        ].each {
            def ctn = GData.g().ctn("SELECT * FROM G_SYS_SETTING where mykey=:KEY", [KEY: it[0]])
            if (ctn == 0) {
                GData.modify("G_SYS_SETTING", "insert", [
                        MYKEY  : it[0],
                        MYVALUE: it[1]
                ])
            }
        }
        GData.g().exec("update G_SYS_SETTING set fact_value=myvalue where fact_value is null and myvalue is not null")
        return APIResponse.ok()
    }
}
