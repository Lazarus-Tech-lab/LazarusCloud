package ru.red.lazaruscloud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/style/**")
                .addResourceLocations("classpath:/static/style/");

        registry
                .addResourceHandler("/script/**")
                .addResourceLocations("classpath:/static/script/");
    }
}
