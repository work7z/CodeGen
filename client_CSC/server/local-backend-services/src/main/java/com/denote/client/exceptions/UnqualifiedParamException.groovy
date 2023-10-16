package com.denote.client.exceptions

class UnqualifiedParamException extends RuntimeException {
    UnqualifiedParamException(String str) {
        super("unqualified param: " + str)
    }
}
