package io.github.diovamny.spring.inertia.security;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
class SecureTestController {

    static final AtomicInteger SUBMIT_INVOCATIONS = new AtomicInteger();

    private final Inertia inertia;
    private final AuthenticationManager authenticationManager;

    SecureTestController(Inertia inertia, AuthenticationManager authenticationManager) {
        this.inertia = inertia;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/public")
    public Object publicPage() {
        return inertia.render("Public", Map.of());
    }

    @GetMapping("/secure")
    public Object securePage() {
        return inertia.render("Secure", Map.of());
    }

    @GetMapping("/admin")
    public Object adminPage() {
        return inertia.render("Admin", Map.of());
    }

    @PostMapping("/submit")
    public Object submit() {
        SUBMIT_INVOCATIONS.incrementAndGet();
        return inertia.redirect("/secure");
    }

    /**
     * Current server-side session after the last successful test login.
     * MockMvc pins the pre-login session object, so the test reads the
     * post-fixation session back through this hook (test-only).
     */
    static volatile jakarta.servlet.http.HttpSession LAST_SESSION;

    @PostMapping(value = "/login-json", consumes = "application/json")
    public Object loginJson(@RequestBody Map<String, String> body, HttpServletRequest request,
            HttpServletResponse response) {
        var email = body.getOrDefault("email", "");
        var password = body.getOrDefault("password", "");
        try {
            var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
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
        return inertia.redirect("/secure");
    }
}
