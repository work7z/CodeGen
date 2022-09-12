package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse

class ModifyDataLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def subject = apiRequest.param["subject"];
        Map<String, Object> appendix = apiRequest.param["appendix"];

        return APIResponse.ok();
    }
}
