package com.davorsauer.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by davor on 13/04/15.
 */
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogProperties implements InitializingBean {

    private String title = "";

    private static final String RELOAD_URL_DEFAULT = "blog_reload";
    private String reloadUrl = RELOAD_URL_DEFAULT;

    private String downloadFolder = "";

    @Override
    public void afterPropertiesSet() throws Exception {
        if (reloadUrl == null && reloadUrl.trim().length() == 0) reloadUrl = RELOAD_URL_DEFAULT;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReloadUrl() {
        return reloadUrl;
    }

    public void setReloadUrl(String reloadUrl) {
        this.reloadUrl = reloadUrl;
    }
    public String getDownloadFolder() {
        return downloadFolder;
    }

    public void setDownloadFolder(String downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

}
