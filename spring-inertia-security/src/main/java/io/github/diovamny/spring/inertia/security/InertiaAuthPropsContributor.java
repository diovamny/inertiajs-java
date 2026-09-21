package io.github.diovamny.spring.inertia.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import io.github.diovamny.spring.inertia.spi.InertiaSharedDataContributor;

/**
 * Opt-in shared-props contributor exposing an allowlisted identity summary
 * for presentation only ({@code auth.user} with {@code id}, {@code name} and
 * {@code roles}).
 *
 * <p>Never exposes the {@code Authentication} object, credentials, tokens or
 * internal attributes. Authorization decisions always stay server-side
 * ({@code @PreAuthorize}, URL rules); these props only show or hide UI
 * controls. Register by setting
 * {@code inertia.security.auth-props-enabled=true}.</p>
 */
public class InertiaAuthPropsContributor implements InertiaSharedDataContributor {

    @Override
    public Map<String, Object> contribute(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var auth = new LinkedHashMap<String, Object>();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            auth.put("user", null);
            return Map.of("auth", auth);
        }
        var user = new LinkedHashMap<String, Object>();
        user.put("id", authentication.getName());
        user.put("name", authentication.getName());
        var roles = new ArrayList<String>();
        for (var authority : authentication.getAuthorities()) {
            var role = authority.getAuthority();
            if (role != null && !role.isBlank()) {
                roles.add(role);
            }
        }
        user.put("roles", roles);
        auth.put("user", user);
        return Map.of("auth", auth);
    }
}
