package com.github.goforce.fsmpdfmerge.model;

public class MergeDoc {
    private String contentVersionIds[];
    private String pathOnClient;
    private String title;
    private String description;
    private String targetObjectIds[];

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
    public String[] getTargetObjectIds() {
        return targetObjectIds;
    }
    public void setTargetObjectIds( String[] ids ) {
        this.targetObjectIds = ids;
    }

}