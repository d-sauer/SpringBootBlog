package com.davorsauer.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by davor on 21/06/15.
 */
@Component
@ConfigurationProperties(prefix = "blog.disqus")
public class DisquisProperties implements InitializingBean {

    private String shortname;

    private boolean isSet;

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(Objects.nonNull(shortname)) {
            isSet = true;
        }
    }

    public boolean isSet() {
        return isSet;
    }
}
