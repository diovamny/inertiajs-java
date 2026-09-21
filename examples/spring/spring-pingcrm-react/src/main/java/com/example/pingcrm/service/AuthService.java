package com.example.pingcrm.service;

import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication for the demo, owned by Spring Security: the identity lives
 * in the {@code SecurityContext} (populated by the login controller through
 * the {@code AuthenticationManager} with BCrypt verification).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> currentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }

    /** Shared prop payload for {@code inertia.share("auth", ...)}. */
    public Map<String, Object> authProps() {
        var payload = new LinkedHashMap<String, Object>();
        var user = currentUser().orElse(null);
        if (user == null) {
            payload.put("user", null);
            return payload;
        }
        var userMap = new LinkedHashMap<String, Object>();
        userMap.put("id", user.id);
        userMap.put("first_name", user.firstName);
        userMap.put("last_name", user.lastName);
        userMap.put("email", user.email);
        userMap.put("owner", user.owner);
        payload.put("user", userMap);
        return payload;
    }
}
