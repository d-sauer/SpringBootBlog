package com.davorsauer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by davor on 16/06/15.
 */
@Configuration
@Profile(EhCacheConfiguration.PROFILE)
@ConfigurationProperties(prefix = "blog.ehcache")
public class EhCacheProperties {


}
