package io.github.diovamny.spring.inertia.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.diovamny.spring.inertia.mvc.InertiaInterceptor;
import io.github.diovamny.spring.inertia.mvc.InertiaReturnValueHandler;

/**
 * Registers the Inertia interceptor (header extraction) and the return-value
 * handler for {@code PageObject} returns into the MVC pipeline.
 */
@Configuration(proxyBeanMethods = false)
public class InertiaWebMvcConfigurer implements WebMvcConfigurer {

    private final InertiaInterceptor interceptor;
    private final InertiaReturnValueHandler returnValueHandler;

    public InertiaWebMvcConfigurer(InertiaInterceptor interceptor,
            InertiaReturnValueHandler returnValueHandler) {
        this.interceptor = interceptor;
        this.returnValueHandler = returnValueHandler;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }

    @Override
    public void addReturnValueHandlers(@NonNull List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(returnValueHandler);
    }
}
