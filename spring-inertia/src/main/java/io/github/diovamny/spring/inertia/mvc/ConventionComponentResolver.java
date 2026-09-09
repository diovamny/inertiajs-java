package io.github.diovamny.spring.inertia.mvc;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.internal.InertiaRequestContext;
import io.github.diovamny.spring.inertia.spi.ComponentTransformer;

/**
 * Automatically resolves frontend component names by convention from the
 * executing Spring MVC handler method or URI path when no component name is
 * explicitly provided (e.g. {@code inertia.render(props)}).
 *
 * <pre>
 * UsersController#edit   -> "Users/Edit"
 * UserController#show    -> "User/Show"
 * ProductController#list -> "Product/List"
 * </pre>
 */
public class ConventionComponentResolver implements ComponentTransformer {

    private final InertiaProperties properties;

    public ConventionComponentResolver(InertiaProperties properties) {
        this.properties = properties;
    }

    @Override
    public String transform(String component) {
        String resolved = component;
        if (resolved == null || resolved.isBlank()) {
            resolved = resolveFromContext();
        }

        if (resolved == null || resolved.isBlank()) {
            resolved = "Index";
        }

        var prefix = properties.getConventionRoutingPrefix();
        if (prefix != null && !prefix.isBlank() && !resolved.startsWith(prefix)) {
            resolved = prefix + resolved;
        }

        return resolved;
    }

    private String resolveFromContext() {
        var request = InertiaRequestContext.request();
        if (request != null) {
            var handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod handlerMethod) {
                var className = handlerMethod.getBeanType().getSimpleName();
                var methodName = handlerMethod.getMethod().getName();
                return formatControllerMethod(className, methodName);
            }

            var path = request.getRequestURI();
            if (path != null && !path.isBlank()) {
                return formatUriPath(path);
            }
        }
        return null;
    }

    private static String formatControllerMethod(String className, String methodName) {
        var controllerName = className;
        if (controllerName.endsWith("Controller")) {
            controllerName = controllerName.substring(0, controllerName.length() - "Controller".length());
        } else if (controllerName.endsWith("Resource")) {
            controllerName = controllerName.substring(0, controllerName.length() - "Resource".length());
        }
        var formattedMethod = capitalize(methodName);
        return controllerName + "/" + formattedMethod;
    }

    private static String formatUriPath(String path) {
        var cleanPath = path.replaceAll("^/+", "").replaceAll("/+$", "");
        if (cleanPath.isBlank()) {
            return "Index";
        }
        var segments = cleanPath.split("/");
        var sb = new StringBuilder();
        for (var segment : segments) {
            if (segment.matches("\\d+") || segment.matches("[0-9a-fA-F-]{36}")) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append("/");
            }
            sb.append(capitalize(segment));
        }
        return sb.isEmpty() ? "Index" : sb.toString();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
