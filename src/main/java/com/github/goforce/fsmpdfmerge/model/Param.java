package com.github.goforce.fsmpdfmerge.model;

public class Param {
    private String serviceUrl;
    private String sessionId;
    private String contentDocument1Id;
    private String contentDocument2Id;
    private String workOrderId;

    public String getServiceUrl() {
        return serviceUrl;
    }
    public void setServiceUrl(String url) {
        this.serviceUrl = url;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String id) {
        this.sessionId = id;
    }
    public String getContentDocument1Id() {
        return contentDocument1Id;
    }
    public void setContentDocument1Id(String id) {
        this.contentDocument1Id = id;
    }
    public String getContentDocument2Id() {
        return contentDocument2Id;
    }
    public void setContentDocument2Id(String id) {
        this.contentDocument2Id = id;
    }
    public String getWorkOrderId() {
        return workOrderId;
    }
    public void setWorkOrderId(String id) {
        this.workOrderId = id;
    }
}