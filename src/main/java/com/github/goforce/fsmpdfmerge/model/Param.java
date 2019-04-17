package com.github.goforce.fsmpdfmerge.model;

public class Param {
    private String serviceUrl;
    private String sessionId;
    private String contentVersionIds[];
    private String pathOnClient;
    private String title;
    private String description;

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
    public String[] getContentVersionIds() {
        return contentVersionIds;
    }
    public void setContentVersionIds( String[] ids ) {
        this.contentVersionIds = ids;
    }
    public String getPathOnClient() {
        return pathOnClient;
    }
    public void setPathOnClient( String pathOnClient ) {
        this.pathOnClient = pathOnClient;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle( String title ) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription( String description ) {
        this.description = description;
    }

}