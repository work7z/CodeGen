package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData

class ExampleLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def preResultForConfig = GData.runTemplateUpdate(apiRequest, "logging-", "G_INFRA_LOGGING");
        if (preResultForConfig.resOk()) {
            return preResultForConfig;
        }
        return APIResponse.noSolution();
    }
}
