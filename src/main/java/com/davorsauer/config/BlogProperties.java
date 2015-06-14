package com.davorsauer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by davor on 13/04/15.
 */
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {

    private String title = "";

    private String downloadFolder = "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

    public void setDownloadFolder(String downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

}
