package com.davorsauer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by davor on 13/04/15.
 */
@ConfigurationProperties("blog")
public class BlogProperties {

    private String name = "";

    private String downloadFolder = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

    public void setDownloadFolder(String downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

}