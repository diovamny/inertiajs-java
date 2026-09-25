package com.example.pingcrm.service;

import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public com.example.pingcrm.entity.Account currentAccount() {
        var user = currentUser().orElse(null);
        if (user == null || user.accountId == null) {
            return null;
        }
        return accountRepository.findById(user.accountId).orElse(null);
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
        var account = currentAccount();
        var userMap = new LinkedHashMap<String, Object>();
        userMap.put("id", user.id);
        userMap.put("first_name", user.firstName);
        userMap.put("last_name", user.lastName);
        userMap.put("email", user.email);
        userMap.put("owner", user.owner);
        var accountMap = new LinkedHashMap<String, Object>();
        accountMap.put("id", account != null ? account.id : null);
        accountMap.put("name", account != null ? account.name : null);
        userMap.put("account", accountMap);
        payload.put("user", userMap);
        return payload;
    }
}
