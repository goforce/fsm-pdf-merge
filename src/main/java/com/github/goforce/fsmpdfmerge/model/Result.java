package com.github.goforce.fsmpdfmerge.model;

public class Result {
    private String contentVersionIds[];
    private String error;

    public String[] getContentVersionIds() {
        return contentVersionIds;
    }
    public void setContentVersionIds( String[] ids ) {
        this.contentVersionIds = ids;
    }
    public String getError() {
        return error;
    }
    public void setError( String err ) {
        this.error = err;
    }

}