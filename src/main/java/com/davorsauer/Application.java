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
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }
}
