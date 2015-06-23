package com.davorsauer.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by davor on 21/06/15.
 */
@Component
@ConfigurationProperties(prefix = "blog.ga")
public class GoogleAnalyticsProperties implements InitializingBean {

    private String id;

    private String domain;

    private boolean isSet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSet() {
        return isSet;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Objects.nonNull(id) && Objects.nonNull(domain)) {
            isSet = true;
        }
    }
}
