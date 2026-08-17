package com.example.pingcrm.config;

import java.util.List;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import com.example.pingcrm.service.AuthService;
import io.github.dg.quarkus.inertia.api.Inertia;

/**
 * Guards every route except public ones (login, logout, assets, images).
 * Unauthenticated requests are redirected to /login; Inertia non-GET requests
 * get a 303 with X-Inertia-Location so the client follows with a GET.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final List<String> PUBLIC_PREFIXES = List.of("assets", "img", "build");
    private static final List<String> PUBLIC_EXACT = List.of("login", "logout", "favicon.svg");

    @Inject
    AuthService auth;

    @Inject
    Inertia inertia;

    @Override
    public void filter(ContainerRequestContext context) {
        var path = context.getUriInfo().getPath();
        var normalized = path.startsWith("/") ? path.substring(1) : path;

        var user = auth.currentUser();
        inertia.share("auth", auth.authProps());

        if (isPublic(normalized)) {
            return;
        }

        if ("login".equals(normalized) && user != null) {
            context.abortWith(Response.status(Response.Status.FOUND)
                .header("Location", "/")
                .build());
            return;
        }

        if (user != null) {
            return;
        }

        var isInertia = "true".equalsIgnoreCase(context.getHeaderString("X-Inertia"));
        if (isInertia && !"GET".equalsIgnoreCase(context.getMethod())) {
            context.abortWith(Response.status(Response.Status.SEE_OTHER)
                .header("Location", "/login")
                .header("X-Inertia-Location", "/login")
                .build());
        } else {
            context.abortWith(Response.status(Response.Status.FOUND)
                .header("Location", "/login")
                .build());
        }
    }

    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path)) return true;
        for (var prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
