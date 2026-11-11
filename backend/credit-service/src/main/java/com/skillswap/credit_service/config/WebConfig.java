package com.skillswap.credit_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String cleInterne;

    public WebConfig(@Value("${skillswap.internal.key}") String cleInterne) {
        this.cleInterne = cleInterne;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InternalKeyInterceptor(cleInterne)).addPathPatterns("/internal/**");
    }
}
