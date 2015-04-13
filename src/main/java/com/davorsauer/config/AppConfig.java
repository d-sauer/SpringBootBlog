package com.davorsauer.config;


import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by davor on 11/04/15.
 */
@Configuration
public class AppConfig extends WebMvcConfigurerAdapter {


    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/webapp/resources/");
    }

}


