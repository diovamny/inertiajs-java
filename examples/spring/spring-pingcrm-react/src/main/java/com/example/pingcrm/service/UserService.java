package com.example.pingcrm.service;

import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> page(Long accountId, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return users.findByAccountIdAndSearch(accountId, search, pageable);
        }
        return users.findByAccountId(accountId, pageable);
    }

    public User findById(Long id) {
        return users.findById(id).orElse(null);
    }

    public void create(User user) {
        if (user.password != null) {
            user.password = passwordEncoder.encode(user.password);
        }
        users.save(user);
    }

    public boolean existsByAccountIdAndEmail(Long accountId, String email) {
        return users.findByAccountIdAndEmail(accountId, email).isPresent();
    }

    public User create(Long accountId, java.util.Map<String, Object> values) {
        var user = new User();
        user.accountId = accountId;
        apply(user, values);
        var password = (String) values.get("password");
        user.password = password != null && !password.isBlank()
            ? passwordEncoder.encode(password)
            : null;
        user.createdAt = java.time.Instant.now();
        user.updatedAt = user.createdAt;
        users.save(user);
        return user;
    }

    public void update(User user) {
        users.save(user);
    }

    public void update(User user, java.util.Map<String, Object> values) {
        apply(user, values);
        var password = (String) values.get("password");
        if (password != null && !password.isBlank()) {
            user.password = passwordEncoder.encode(password);
        }
        user.updatedAt = java.time.Instant.now();
        users.save(user);
    }

    private void apply(User user, java.util.Map<String, Object> values) {
        user.firstName = (String) values.get("first_name");
        user.lastName = (String) values.get("last_name");
        user.email = (String) values.get("email");
        var owner = values.get("owner");
        user.owner = Boolean.TRUE.equals(owner)
            || "true".equalsIgnoreCase(String.valueOf(owner));
    }

    public void softDelete(User user) {
        user.deletedAt = java.time.Instant.now();
        users.save(user);
    }

    public void restore(User user) {
        user.deletedAt = null;
        users.save(user);
    }
}
