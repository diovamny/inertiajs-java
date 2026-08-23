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

    public void update(User user) {
        users.save(user);
    }

    public void softDelete(User user) {
        user.deletedAt = java.time.LocalDateTime.now();
        users.save(user);
    }

    public void restore(User user) {
        user.deletedAt = null;
        users.save(user);
    }
}