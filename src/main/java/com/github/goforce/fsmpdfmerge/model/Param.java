package com.github.goforce.fsmpdfmerge.model;

public class Param {
    private String serviceUrl;
    private String sessionId;
    private MergeDoc mergeDocs[];

    public String getServiceUrl() {
        return serviceUrl;
    }
    public void setServiceUrl( String url ) {
        this.serviceUrl = url;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId( String id ) {
        this.sessionId = id;
    }
    public MergeDoc[] getMergeDocs() {
        return mergeDocs;
    }
    public void setMergeDocs( MergeDoc[] docs ) {
        this.mergeDocs = docs;
    }

}