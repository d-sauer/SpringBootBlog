package com.davorsauer.domain;

/**
 * Created by davor on 20/06/15.
 */
public class ContentData {

    private String content;

    private ArticleMetadata metadata;

    public ContentData() {

    }

    public ContentData(String content, ArticleMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArticleMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ArticleMetadata metadata) {
        this.metadata = metadata;
    }
}
