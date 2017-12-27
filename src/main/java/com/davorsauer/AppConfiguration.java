package com.davorsauer;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;


/**
 * Created by davor on 30/06/15.
 */
@Configuration
@ComponentScan(basePackageClasses = {com.davorsauer.config.AppConfig.class,
        com.davorsauer.controller.WebController.class,
        com.davorsauer.service.SendMailService.class})
//@EnableCaching
public class AppConfiguration {

    @Bean
    public CacheManager cacheManager() {
//                CachingProvider provider = Caching.getCachingProvider();
        CachingProvider provider = new EhcacheCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        return cacheManager;
    }


}
