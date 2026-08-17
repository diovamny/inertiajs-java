package io.github.dg.examples.springdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.dg.examples.springdemo.config.SharedPropsInterceptor;

@Configuration
public class InertiaDemoConfig implements WebMvcConfigurer {

    private final SharedPropsInterceptor sharedPropsInterceptor;

    public InertiaDemoConfig(SharedPropsInterceptor sharedPropsInterceptor) {
        this.sharedPropsInterceptor = sharedPropsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sharedPropsInterceptor);
    }
}