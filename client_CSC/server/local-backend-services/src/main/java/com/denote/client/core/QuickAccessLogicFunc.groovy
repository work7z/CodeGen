package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData

class QuickAccessLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def allowArr = [
                ['rule-', 'g_proxy_config_rule'],
                ['path-rewrite-of-rule', 'g_proxy_config_rules_path_rewrite']
        ]
        for (def a : allowArr) {
            def preResultForFolder = GData.runTemplateUpdate(apiRequest, a[0], a[1]);
            if (preResultForFolder.resOk()) {
                return preResultForFolder;
            }
        }
        return APIResponse.ok();
    }
}
