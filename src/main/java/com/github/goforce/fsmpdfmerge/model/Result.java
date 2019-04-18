package com.github.goforce.fsmpdfmerge.model;

public class Result {
    private String contentVersionId;
    private String error;

    public String getContentVersionId() {
        return contentVersionId;
    }
    public void setContentVersionId( String id ) {
        this.contentVersionId = id;
    }
    public String getError() {
        return error;
    }
    public void setError( String err ) {
        this.error = err;
    }

}