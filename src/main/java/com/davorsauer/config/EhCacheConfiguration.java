package com.davorsauer.config;

import org.ehcache.CacheManagerBuilder;
import org.ehcache.config.CacheConfigurationBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by davor on 16/06/15.
 * Example with EhCache 3.0, http://ehcache.github.io/
 */
@Configuration
@EnableCaching
@Profile(EhCacheConfiguration.PROFILE)
public class EhCacheConfiguration {

    public static final String PROFILE = "ehcache";

    @Bean
    public CacheManager cacheManager() {
        org.ehcache.CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder()
                                .buildConfig(Long.class, String.class))
                .build();
        return (CacheManager) cacheManager;
    }

}
