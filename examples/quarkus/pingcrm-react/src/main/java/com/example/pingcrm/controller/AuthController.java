package com.example.pingcrm.controller;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.LoginForm;
import com.example.pingcrm.repository.UserRepository;
import com.example.pingcrm.service.AuthService;
import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/")

@Blocking
public class AuthController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    UserRepository userRepository;

    @Inject
    Validator validator;

    @GET
    @Path("login")
    @Blocking
    public Uni<Object> create() {
        if (auth.currentUser() != null) {
            return inertia.redirect("/");
        }
        return inertia.render("Auth/Login");
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(LoginForm form) {
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);
        FormValidator.validate(validator, form);

        var user = form.email != null ? userRepository.findByEmail(form.email) : null;
        if (user == null || user.deletedAt != null || user.password == null
                || !AuthService.matches(form.password, user.password)) {
            return inertia.back("/login").withErrors(Map.of("email", "These credentials do not match our records."));
        }

        auth.login(user);
        return inertia.redirect("/");
    }

    @DELETE
    @Path("logout")
    @Blocking
    public Response destroy() {
        auth.logout();
        return (Response) inertia.redirect("/login").await().indefinitely();
    }
}
