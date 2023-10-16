package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse

abstract class BasicLogicFunc {
    abstract APIResponse handle(APIRequest apiRequest);
}
