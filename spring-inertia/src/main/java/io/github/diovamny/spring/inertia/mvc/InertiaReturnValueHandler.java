package io.github.diovamny.spring.inertia.mvc;

import org.springframework.core.MethodParameter;
import org.jspecify.annotations.NonNull;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.github.diovamny.inertia.core.model.PageObject;
import io.github.diovamny.inertia.core.result.InertiaLocationResult;
import io.github.diovamny.inertia.core.result.InertiaPageResult;
import io.github.diovamny.inertia.core.result.InertiaRedirectResult;
import io.github.diovamny.inertia.core.result.InertiaResult;
import io.github.diovamny.spring.inertia.protocol.RedirectProcessor;
import io.github.diovamny.spring.inertia.protocol.ResponseProcessor;

/**
 * Serves controller methods returning a {@link PageObject} or a typed
 * {@link InertiaResult} directly. Values are processed through the
 * {@link ResponseProcessor} (Inertia JSON or full HTML) and written to the
 * response.
 *
 * <p>{@code InertiaResponse}/{@code InertiaRedirect} need no custom handler:
 * they extend {@code ResponseEntity} and are served natively by Spring
 * MVC.</p>
 */
public class InertiaReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final ResponseProcessor responseProcessor;
    private final RedirectProcessor redirectProcessor;

    public InertiaReturnValueHandler(ResponseProcessor responseProcessor) {
        this(responseProcessor, null);
    }

    public InertiaReturnValueHandler(ResponseProcessor responseProcessor,
            RedirectProcessor redirectProcessor) {
        this.responseProcessor = responseProcessor;
        this.redirectProcessor = redirectProcessor;
    }

    @Override
    public boolean supportsReturnType(@NonNull MethodParameter returnType) {
        return PageObject.class.isAssignableFrom(returnType.getParameterType())
            || InertiaResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, @NonNull MethodParameter returnType,
            @NonNull ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        var response = toResponse(returnValue);
        if (response == null) {
            return;
        }
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

    private org.springframework.http.ResponseEntity<String> toResponse(Object returnValue) {
        if (returnValue instanceof PageObject page) {
            return responseProcessor.process(page);
        }
        if (returnValue instanceof InertiaPageResult page) {
            return responseProcessor.process(page.component(), page.props());
        }
        if (returnValue instanceof InertiaRedirectResult redirect) {
            return redirectProcessor != null
                ? redirectProcessor.redirect(redirect.url(), redirect.fullPage())
                : null;
        }
        if (returnValue instanceof InertiaLocationResult location) {
            return redirectProcessor != null ? redirectProcessor.location(location.url()) : null;
        }
        return null;
    }
}
