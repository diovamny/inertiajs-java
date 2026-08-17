package io.github.dg.examples.springdemo.config;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.dg.spring.inertia.api.Inertia;

@Component
public class SharedPropsInterceptor implements HandlerInterceptor {

    private final Inertia inertia;

    public SharedPropsInterceptor(Inertia inertia) {
        this.inertia = inertia;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        inertia.share("app", Map.of(
            "name", "spring-demo",
            "framework", "Spring Boot 4.1 + Inertia.js v3"
        ));
        return true;
    }
}