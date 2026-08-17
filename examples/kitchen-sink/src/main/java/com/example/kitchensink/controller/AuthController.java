package com.example.kitchensink.controller;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.LoginForm;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.AuthService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.vertx.ext.web.RoutingContext;

@Path("/")
public class AuthController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    UserRepository users;

    @Inject
    jakarta.validation.Validator validator;

    @GET
    @Blocking
    public Uni<Object> home() {
        return inertia.redirect("/login");
    }

    @GET
    @Path("login")
    @Blocking
    public Uni<Object> loginPage() {
        var status = inertia.pullFlash("status", null);
        var props = new java.util.LinkedHashMap<String, Object>();
        props.put("status", status);
        return inertia.render("Auth/Login", props);
    }

    @POST
    @Path("login")
    @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> login(LoginForm form) {
        FormValidator.validate(validator, form);
        var user = users.findByEmail(form.email);
        if (user == null || form.password == null || form.password.isBlank()) {
            return inertia.back().withErrors(Map.of("email", "These credentials do not match our records."));
        }
        auth.login(user);
        return inertia.redirect("/dashboard");
    }

    @POST
    @Path("logout")
    @Blocking
    public Uni<Object> logout() {
        auth.logout();
        return inertia.redirect("/login");
    }
}
