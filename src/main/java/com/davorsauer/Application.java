package com.davorsauer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.WebApplicationInitializer;

/**
 * Created by davor on 11/04/15.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {com.davorsauer.config.AppConfig.class,
                                     com.davorsauer.controller.WebController.class,
                                     com.davorsauer.service.SendMailService.class})
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }
}
