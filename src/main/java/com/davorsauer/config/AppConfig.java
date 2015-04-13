package com.davorsauer.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by davor on 11/04/15.
 */
@Configuration
public class AppConfig extends WebMvcConfigurerAdapter {

//    @Bean
//    @Description("Thymeleaf template resolver serving HTML 5")
//    public ServletContextTemplateResolver templateResolver() {
//        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
//        templateResolver.setPrefix("/WEB-INF/views/");
//        templateResolver.setSuffix(".html");
//        templateResolver.setTemplateMode("HTML5");
//        templateResolver.setCharacterEncoding("UTF-8");
//
//        templateResolver.setCacheable(false);
//        templateResolver.setCacheTTLMs(0L);
//
//        return templateResolver;
//    }
//
//    @Bean
//    @Description("Thymeleaf template engine with Spring integration")
//    public SpringTemplateEngine templateEngine() {
//        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//        templateEngine.setTemplateResolver(templateResolver());
//
//        templateEngine.addDialect(new LayoutDialect());
//
//        return templateEngine;
//    }
//
//    @Bean
//    @Description("Thymeleaf view resolver")
//    public ThymeleafViewResolver viewResolver() {
//        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
//        viewResolver.setCharacterEncoding("UTF-8");
//        viewResolver.setTemplateEngine(templateEngine());
//
//        viewResolver.setCache(false);
//        viewResolver.setCacheLimit(0);
//        viewResolver.setCacheUnresolved(false);
//
//        return viewResolver;
//    }
//
//    @Bean
//    @Description("Spring message resolver")
//    public ResourceBundleMessageSource messageSource() {
//        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
//        messageSource.setBasename("i18n/messages");
//
//        return messageSource;
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
//    }

}


