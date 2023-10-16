package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.CalcDateUtils
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils

class InternalCallingLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def query = apiRequest.param['query']
        if (query) {
            query.each {
                def create_time = it['CREATE_TIME']
                if (create_time) {
                    def calcStr = CalcDateUtils.getDateCalcStr(create_time, GData.getCurrentLang());
                    it['CREATE_TIME_DESC'] = calcStr;
                    it['CREATE_TIME_STR'] = GUtils.getDateStr(create_time);
                }
            }
        }
        return APIResponse.ok(query);
    }
}
