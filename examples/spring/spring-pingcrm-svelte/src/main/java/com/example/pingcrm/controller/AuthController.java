package com.example.pingcrm.controller;

import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Validator;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.LoginForm;
import com.example.pingcrm.repository.UserRepository;
import com.example.pingcrm.service.AuthService;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class AuthController {

    public static final String SESSION_USER_KEY = "pingcrm.userId";

    private final Inertia inertia;
    private final AuthService auth;
    private final UserRepository userRepository;
    private final Validator validator;

    public AuthController(Inertia inertia, AuthService auth, UserRepository userRepository, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @GetMapping("/login")
    public Object create() {
        if (auth.currentUser() != null) {
            return inertia.redirect("/");
        }
        return inertia.render("Auth/Login", Map.of());
    }

    @PostMapping("/login")
    public Object store(@RequestBody LoginForm form, HttpSession session) {
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);
        FormValidator.validate(validator, form);

        Optional<com.example.pingcrm.entity.User> userOpt = form.email != null ? userRepository.findByEmail(form.email) : Optional.empty();
        var user = userOpt.orElse(null);
        if (user == null || user.deletedAt != null || user.password == null
                || !AuthService.matches(form.password, user.password)) {
            return inertia.back()
                .withErrors(Map.of("email", "These credentials do not match our records."));
        }

        session.setAttribute(SESSION_USER_KEY, user.id);
        return inertia.location("/");
    }

    @DeleteMapping("/logout")
    public Object destroy(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        return inertia.redirect("/login");
    }
}
