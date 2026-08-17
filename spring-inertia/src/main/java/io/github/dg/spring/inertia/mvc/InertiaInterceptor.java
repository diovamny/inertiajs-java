package io.github.dg.spring.inertia.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.dg.spring.inertia.protocol.InertiaHeaderExtractor;

/**
 * Runs before every controller: extracts the Inertia protocol headers into
 * the request attributes used by the whole pipeline.
 */
public class InertiaInterceptor implements HandlerInterceptor {

    private final InertiaHeaderExtractor headerExtractor;

    public InertiaInterceptor(InertiaHeaderExtractor headerExtractor) {
        this.headerExtractor = headerExtractor;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        headerExtractor.extract(request);
        return true;
    }
}