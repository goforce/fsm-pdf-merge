package com.github.goforce.fsmpdfmerge.model;

public class Result {
    private String contentVersionIds[];
    private String errorMessage;

    public String[] getContentVersionIds() {
        return contentVersionIds;
    }
    public void setContentVersionIds( String[] ids ) {
        this.contentVersionIds = ids;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage( String err ) {
        this.errorMessage = err;
    }

}