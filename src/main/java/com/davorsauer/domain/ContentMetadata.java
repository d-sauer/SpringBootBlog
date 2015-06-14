package com.davorsauer.domain;

import com.davorsauer.commons.ContentMetadataUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.Set;

/**
 * Created by davor on 05/05/15.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class ContentMetadata {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publishDate;

    private Boolean published;

    private Set<String> tags;

    private String title;

    private String description;

    private String template;

    public Date getPublishDate() {
        return publishDate;
    }

    public String getFormatPublishDate() {
        return ContentMetadataUtils.getFormatPublishDate(this, null);
    }

    public String getFormatPublishDate(String format) {
        return ContentMetadataUtils.getFormatPublishDate(this, format);
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Boolean isPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
