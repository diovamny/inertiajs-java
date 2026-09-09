package io.github.diovamny.quarkus.inertia.protocol;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;
import io.github.diovamny.quarkus.inertia.spi.ComponentTransformer;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Automatically resolves frontend component names by convention from the
 * Vert.x / JAX-RS request path when no component name is explicitly passed.
 *
 * <pre>
 * /users/edit -> "Users/Edit"
 * /dashboard  -> "Dashboard"
 * </pre>
 */
@ApplicationScoped
public class ConventionComponentResolver implements ComponentTransformer {

    @Inject
    InertiaConfig config;

    @Inject
    jakarta.enterprise.inject.Instance<RoutingContext> routingContextInstance;

    @Override
    public String transform(String component) {
        String resolved = component;
        if (resolved == null || resolved.isBlank()) {
            resolved = resolveFromContext();
        }

        if (resolved == null || resolved.isBlank()) {
            resolved = "Index";
        }

        var prefix = config.conventionRoutingPrefix().orElse("");
        if (!prefix.isBlank() && !resolved.startsWith(prefix)) {
            resolved = prefix + resolved;
        }

        return resolved;
    }

    private String resolveFromContext() {
        if (routingContextInstance.isResolvable()) {
            var ctx = routingContextInstance.get();
            if (ctx != null && ctx.request() != null) {
                var path = ctx.request().path();
                return formatUriPath(path);
            }
        }
        return null;
    }

    private static String formatUriPath(String path) {
        if (path == null) {
            return "Index";
        }
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
