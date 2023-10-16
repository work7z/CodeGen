package com.denote.client.core

import com.alibaba.druid.pool.DruidDataSource
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController

class QuickTestLocalDriverLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        return APIResponse.ok()
    }

    static void main(String[] args) {
        GlobalController.call(QuickTestLocalDriverLogicFunc.class, new APIRequest())
    }
}
