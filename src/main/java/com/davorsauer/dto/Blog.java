package com.davorsauer.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by davor on 13/04/15.
 */
public class Blog {

    private String html;

    private Set<String> tags = new HashSet<>();


    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}


