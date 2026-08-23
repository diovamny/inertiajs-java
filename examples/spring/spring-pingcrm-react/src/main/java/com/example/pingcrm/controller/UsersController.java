package com.example.pingcrm.controller;

import com.example.pingcrm.dto.UserForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.UserService;
import io.github.dg.spring.inertia.api.Inertia;
import jakarta.validation.Validator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UsersController {

    private final Inertia inertia;
    private final AuthService auth;
    private final UserService users;
    private final Validator validator;

    public UsersController(Inertia inertia, AuthService auth, UserService users, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.users = users;
        this.validator = validator;
    }

    @GetMapping
    public Object index(@RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "1") int page) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var result = users.page(accountId, search, PageRequest.of(page - 1, 10, Sort.by("id").descending()));
        return inertia.render("Users/Index", Map.of(
            "filters", Map.of("search", search),
            "users", result));
    }

    @GetMapping("/create")
    public Object create() {
        return inertia.render("Users/Create");
    }

    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object store(@ModelAttribute UserForm form) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        FormValidator.normalize(form);
        FormValidator.validate(validator, form);
        if (users.existsByAccountIdAndEmail(accountId, form.email)) {
            return inertia.back().withErrors(Map.of("email", "The email has already been taken."));
        }
        users.create(accountId, FormValidator.toValues(form));
        return inertia.redirect("/users").with("success", "User created.");
    }

    @GetMapping("/{id}/edit")
    public Object edit(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var user = users.findById(id);
        if (user == null || !user.accountId.equals(accountId)) {
            return notFound();
        }
        return inertia.render("Users/Edit", Map.of("user", user));
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object update(@PathVariable long id, @ModelAttribute UserForm form) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var user = users.findById(id);
        if (user == null || !user.accountId.equals(accountId)) {
            return notFound();
        }
        FormValidator.normalize(form);
        FormValidator.validate(validator, form);
        users.update(user, FormValidator.toValues(form));
        return inertia.back().with("success", "User updated.");
    }

    @DeleteMapping("/{id}")
    public Object destroy(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var user = users.findById(id);
        if (user == null || !user.accountId.equals(accountId)) {
            return notFound();
        }
        if (user.owner) {
            return inertia.back().with("error", "The demo user cannot be deleted.");
        }
        users.softDelete(user);
        return inertia.back().with("success", "User deleted.");
    }

    @PutMapping("/{id}/restore")
    public Object restore(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var user = users.findById(id);
        if (user == null || !user.accountId.equals(accountId)) {
            return notFound();
        }
        users.restore(user);
        return inertia.back().with("success", "User restored.");
    }

    private Object notFound() {
        return inertia.redirect("/users").with("error", "User not found.");
    }
}