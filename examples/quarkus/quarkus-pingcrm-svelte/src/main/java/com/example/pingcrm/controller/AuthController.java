package com.example.pingcrm.controller;

import com.example.pingcrm.dto.LoginForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.repository.UserRepository;
import com.example.pingcrm.service.AuthService;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.Param;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

@RouteBase(path = "/")
public class AuthController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    UserRepository userRepository;

    @Inject
    Validator validator;

    @Route(path = "login", methods = Route.HttpMethod.GET)
    public Uni<Object> create() {
        return auth.currentUser()
            .onItem().transform(user -> {
                if (user != null) {
                    return inertia.redirect("/");
                }
                return inertia.render("Auth/Login");
            });
    }

    @Route(path = "login", methods = Route.HttpMethod.POST)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> store(LoginForm form, RoutingContext rc) {
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);

        // Validation
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            var errors = new java.util.LinkedHashMap<String, String>();
            for (var v : violations) {
                errors.put(v.getPropertyPath().toString(), v.getMessage());
            }
            return Uni.createFrom().item(
                inertia.back("/login").withErrors(errors).toResponse()
            );
        }

        return userRepository.findByEmail(form.email)
            .onItem().transformToUni(user -> {
                if (user == null || user.deletedAt != null || user.password == null
                        || !AuthService.matches(form.password, user.password)) {
                    return Uni.createFrom().item(
                        inertia.back("/login").withErrors(Map.of("email", "These credentials do not match our records.")).toResponse()
                    );
                }

                // Login successful - set session via Quarkus Security
                return auth.login(user)
                    .replaceWith(inertia.redirect("/"));
            });
    }

    @Route(path = "logout", methods = Route.HttpMethod.DELETE)
    public Uni<Object> destroy() {
        return auth.logout()
            .replaceWith(inertia.redirect("/login"));
    }
}