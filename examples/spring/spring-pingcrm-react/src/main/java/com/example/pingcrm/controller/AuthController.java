package com.example.pingcrm.controller;

import com.example.pingcrm.dto.LoginForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.repository.UserRepository;
import com.example.pingcrm.service.AuthService;
import io.github.diovamny.spring.inertia.api.Inertia;
import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AuthController {

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
        if (auth.currentUser().isPresent()) {
            return inertia.redirect("/");
        }
        return inertia.render("Auth/Login");
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody LoginForm form) {
        FormValidator.blankToNull(form.email);
        FormValidator.blankToNull(form.password);
        FormValidator.validate(validator, form);

        var user = form.email != null ? userRepository.findByEmail(form.email).orElse(null) : null;
        if (user == null || user.deletedAt != null || user.password == null
                || !AuthService.matches(form.password, user.password)) {
            return inertia.back("/login").withErrors(Map.of("email", "These credentials do not match our records."));
        }

        auth.login(user);
        return inertia.redirect("/");
    }

    @DeleteMapping("/logout")
    public Object destroy() {
        auth.logout();
        return inertia.redirect("/login");
    }
}
