package com.denote.client.dto

import lombok.Data
import lombok.Getter
import lombok.Setter

import javax.servlet.ServletConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Data
@Getter
@Setter
class APIRequest {
    private String actionCategory;
    private String actionType;
    private boolean needGenerateID = false;
    private Map<String, Object> param = [:];

    APIRequest() {
    }

    APIRequest(Map<String, Object> param) {
        this.param = param
    }

    APIRequest(String actionType, Map<String, Object> param) {
        this.actionType = actionType
        this.param = param
    }

    APIRequest(String actionCategory, String actionType, Map<String, Object> param) {
        this.actionCategory = actionCategory
        this.actionType = actionType
        this.param = param
    }

    public HttpServletRequest getReq() {
        return param['req'] as HttpServletRequest
    }

    public HttpServletResponse getRes() {
        return param['res'] as HttpServletResponse
    }

    public ServletConfig getConfig() {
        return param['servletConfig'] as ServletConfig
    }

    static void main(String[] args) {
    }

    String getActionCategory() {
        return actionCategory
    }

    void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory
    }

    String getActionType() {
        return actionType
    }

    void setActionType(String actionType) {
        this.actionType = actionType
    }

    boolean getNeedGenerateID() {
        return needGenerateID
    }

    void setNeedGenerateID(boolean needGenerateID) {
        this.needGenerateID = needGenerateID
    }

    Map<String, Object> getParam() {
        return param
    }

    void setParam(Map<String, Object> param) {
        this.param = param
    }
}
