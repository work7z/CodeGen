package com.denote.client.core

import com.denote.client.dto.APIRequest

class JustTestFunc {
    void test() {
        println "you got me"
        APIRequest apiRequest = new APIRequest("libai", "kkk", ['k': 12])
        println apiRequest.param
        println apiRequest
        println "good"
    }
}
