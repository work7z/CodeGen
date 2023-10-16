package com.denote.client.dto

class APIResponse {
    int status; // 1 normal, another code is error code
    String message; // message for current response
    Object content;
    long timestamp = Calendar.getInstance().getTime().getTime();

    public static APIResponse ok(String message, Object content) {
        return new APIResponse(1, message, content);
    }

    public static APIResponse ok(Object content) {
        return new APIResponse(1, null, content);
    }

    public boolean resOk() {
        return this.status == 1;
    }

    public static APIResponse ok() {
        return new APIResponse(1, null, null);
    }

    public static APIResponse noSolution() {
        return err(-2, "no suitable solution for your request", null)
    }

    public static APIResponse err(int status, String message, Object content) {
        return new APIResponse(status, message, content);
    }


    public static APIResponse err(String message) {
        return new APIResponse(-20, message, null);
    }

    private APIResponse(int status, String message, Object content) {
        this.status = status
        this.message = message
        this.content = content
    }
}
