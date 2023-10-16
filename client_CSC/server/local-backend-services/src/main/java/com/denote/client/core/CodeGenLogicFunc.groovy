package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse

class CodeGenLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        return APIResponse.ok("Got your request", apiRequest);
    }
}
