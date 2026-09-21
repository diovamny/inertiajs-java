package com.example.kitchensink.controller;

import java.util.LinkedHashMap;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.LoginForm;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class AuthController {

    private final Inertia inertia;
    private final Validator validator;
    private final AuthenticationManager authenticationManager;

    public AuthController(Inertia inertia, Validator validator,
            AuthenticationManager authenticationManager) {
        this.inertia = inertia;
        this.validator = validator;
        this.authenticationManager = authenticationManager;
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

    /**
     * Current server-side session after the last successful login. Session
     * fixation rotates the id, so MockMvc tests read the live session back
     * through this hook (test-only; browsers follow cookies transparently).
     */
    public static volatile jakarta.servlet.http.HttpSession LAST_SESSION;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object login(@RequestBody LoginForm form, HttpServletRequest request,
            HttpServletResponse response) {
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
            return inertia.back().withErrors(Map.of("email", "These credentials do not match our records."));
        }
        return inertia.redirect("/dashboard");
    }

    @RequestMapping(path = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
    public Object logout(HttpServletRequest request, HttpServletResponse response) {
        var logout = new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler();
        logout.logout(request, response,
            SecurityContextHolder.getContext().getAuthentication());
        return inertia.redirect("/login");
    }
}
