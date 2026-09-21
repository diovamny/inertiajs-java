package com.example.pingcrm.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.LoginForm;
import com.example.pingcrm.service.AuthService;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class AuthController {

    private final Inertia inertia;
    private final AuthService auth;
    private final Validator validator;
    private final AuthenticationManager authenticationManager;

    public AuthController(Inertia inertia, AuthService auth, Validator validator,
            AuthenticationManager authenticationManager) {
        this.inertia = inertia;
        this.auth = auth;
        this.validator = validator;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Current server-side session after the last successful login. Session
     * fixation rotates the id, so MockMvc tests read the live session back
     * through this hook (test-only; browsers follow cookies transparently).
     */
    public static volatile jakarta.servlet.http.HttpSession LAST_SESSION;

    @GetMapping("/login")
    public Object create() {
        if (auth.currentUser() != null) {
            return inertia.redirect("/");
        }
        return inertia.render("Auth/Login", Map.of());
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody LoginForm form, HttpServletRequest request,
            HttpServletResponse response) {
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);
        FormValidator.validate(validator, form);

        try {
            var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(form.email, form.password));
            new SessionFixationProtectionStrategy().onAuthentication(authentication, request, response);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            var session = request.getSession();
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            LAST_SESSION = session;
        } catch (Exception e) {
            return inertia.back()
                .withErrors(Map.of("email", "These credentials do not match our records."));
        }

        return inertia.redirect("/");
    }

    @DeleteMapping("/logout")
    public Object destroy(HttpServletRequest request, HttpServletResponse response) {
        new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler()
            .logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return inertia.redirect("/login");
    }
}
