package com.example.pingcrm.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.UserForm;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.UserRepository;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.UserService;
import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/users")

@Blocking
public class UsersController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    UserService users;

    @Inject
    UserRepository userRepository;

    @Inject
    Validator validator;

    @GET
    @Blocking
    public Uni<Object> index(@QueryParam("search") String search,
            @QueryParam("role") String role,
            @QueryParam("trashed") String trashed) {
        var list = users.list(auth.accountId(), search, role, trashed);
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("role", role);
        filters.put("trashed", trashed);
        return inertia.render("Users/Index", Map.of(
            "filters", filters,
            "users", list));
    }

    @GET
    @Path("create")
    public Uni<Object> create() {
        return inertia.render("Users/Create");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(UserForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        if (userRepository.emailExistsForOtherUser(form.email, null)) {
            return emailTaken();
        }
        users.create(auth.accountId(), form.first_name, form.last_name, form.email,
            form.password, Boolean.TRUE.equals(form.owner), null);
        return inertia.redirect("/users").with("success", "User created.");
    }

    @GET
    @Path("{id}/edit")
    @Blocking
    public Uni<Object> edit(@PathParam("id") long id) {
        var user = findOwned(id);
        if (user == null) {
            return notFound();
        }
        return inertia.render("Users/Edit", Map.of("user", user));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> update(@PathParam("id") long id, UserForm form) {
        var user = findOwned(id);
        if (user == null) {
            return notFound();
        }
        if (user.isDemoUser()) {
            return inertia.back().with("error", "Updating the demo user is not allowed.");
        }
        normalize(form);
        FormValidator.validate(validator, form);
        if (userRepository.emailExistsForOtherUser(form.email, user.id)) {
            return emailTaken();
        }
        users.update(user, form.first_name, form.last_name, form.email,
            form.password, Boolean.TRUE.equals(form.owner), null);
        return inertia.back().with("success", "User updated.");
    }

    @DELETE
    @Path("{id}")
    @Blocking
    public Uni<Object> destroy(@PathParam("id") long id) {
        var user = findOwned(id);
        if (user == null) {
            return notFound();
        }
        if (user.isDemoUser()) {
            return inertia.back().with("error", "Deleting the demo user is not allowed.");
        }
        users.softDelete(user);
        return inertia.back().with("success", "User deleted.");
    }

    @PUT
    @Path("{id}/restore")
    @Blocking
    public Uni<Object> restore(@PathParam("id") long id) {
        var user = userRepository.findByIdWithTrashed(id);
        if (user == null || !user.accountId.equals(auth.accountId())) {
            return notFound();
        }
        users.restore(user);
        return inertia.back().with("success", "User restored.");
    }

    private User findOwned(long id) {
        var user = userRepository.findByIdWithTrashed(id);
        if (user == null || !user.accountId.equals(auth.accountId())) {
            return null;
        }
        return user;
    }

    private Uni<Object> notFound() {
        return inertia.redirect("/users").with("error", "User not found.");
    }

    private Uni<Object> emailTaken() {
        return inertia.back().withErrors(Map.of("email", "The email has already been taken."));
    }

    private void normalize(UserForm form) {
        form.first_name = FormValidator.blankToNull(form.first_name);
        form.last_name = FormValidator.blankToNull(form.last_name);
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);
    }
}
