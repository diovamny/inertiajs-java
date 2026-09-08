package com.example.kitchensink.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.LoginForm;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.AuthService;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class AuthController {

    private final Inertia inertia;
    private final AuthService auth;
    private final UserRepository users;
    private final Validator validator;

    public AuthController(Inertia inertia, AuthService auth, UserRepository users, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.users = users;
        this.validator = validator;
    }

    @GetMapping("/")
    public Object home() {
        return inertia.redirect("/login");
    }

    @GetMapping("/login")
    public Object loginPage() {
        var props = new LinkedHashMap<String, Object>();
        props.put("status", null);
        return inertia.render("Auth/Login", props);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object login(@RequestBody LoginForm form) {
        FormValidator.validate(validator, form);
        var user = users.findByEmail(form.email).orElse(null);
        if (user == null || form.password == null || form.password.isBlank()) {
            return inertia.back().withErrors(Map.of("email", "These credentials do not match our records."));
        }
        auth.login(user);
        return inertia.redirect("/dashboard");
    }

    @RequestMapping(path = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public Object logout() {
        auth.logout();
        return inertia.redirect("/login");
    }
}
