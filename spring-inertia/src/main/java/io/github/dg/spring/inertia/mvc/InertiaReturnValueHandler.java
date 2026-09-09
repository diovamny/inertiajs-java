package io.github.dg.spring.inertia.mvc;

import org.springframework.core.MethodParameter;
import org.jspecify.annotations.NonNull;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.protocol.ResponseProcessor;

/**
 * Serves controller methods returning a {@link PageObject} directly. The
 * page is processed through the {@link ResponseProcessor} (Inertia JSON or
 * full HTML) and written to the response.
 *
 * <p>{@code InertiaResponse}/{@code InertiaRedirect} need no custom handler:
 * they extend {@code ResponseEntity} and are served natively by Spring
 * MVC.</p>
 */
public class InertiaReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final ResponseProcessor responseProcessor;

    public InertiaReturnValueHandler(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public boolean supportsReturnType(@NonNull MethodParameter returnType) {
        return PageObject.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, @NonNull MethodParameter returnType,
            @NonNull ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        if (returnValue instanceof PageObject page) {
            var response = responseProcessor.process(page);
            var servletResponse = webRequest.getNativeResponse(jakarta.servlet.http.HttpServletResponse.class);
            if (servletResponse != null) {
                servletResponse.setStatus(response.getStatusCode().value());
                response.getHeaders().forEach((name, values) ->
                    values.forEach(value -> servletResponse.setHeader(name, value)));
                var body = response.getBody();
                if (body != null) {
                    var writer = servletResponse.getWriter();
                    writer.write(body);
                }
            }
        }
    }
}
