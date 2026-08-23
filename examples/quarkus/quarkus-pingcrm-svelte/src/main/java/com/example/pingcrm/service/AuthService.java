package com.example.pingcrm.service;

import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.AccountRepository;
import com.example.pingcrm.repository.UserRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@RequestScoped
public class AuthService {

    static final String SESSION_USER_KEY = "pingcrm.userId";

    private static final int PBKDF2_ITERATIONS = 210_000;
    private static final int PBKDF2_KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    @Inject
    UserRepository userRepository;

    @Inject
    AccountRepository accountRepository;

    private User cached;
    private boolean cachedSet;

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    public Uni<User> currentUser() {
        if (cachedSet) {
            return Uni.createFrom().item(cached);
        }
        
        // In reactive context, get user from security context or session
        // For now, return null - in a real app this would come from security context
        return Uni.createFrom().nullItem();
    }

    public Uni<Account> currentAccount() {
        return currentUser()
            .onItem().transformToUni(user -> {
                if (user == null) return Uni.createFrom().nullItem();
                return accountRepository.findById(user.accountId);
            });
    }

    public Uni<Long> accountId() {
        return currentUser()
            .onItem().transform(user -> user != null ? user.accountId : null);
    }

    public Uni<Void> login(User user) {
        // In reactive Quarkus, session management is handled by Quarkus Security
        // This would integrate with Quarkus Security / JWT
        cached = user;
        cachedSet = true;
        return Uni.createFrom().voidItem();
    }

    public Uni<Void> logout() {
        cached = null;
        cachedSet = true;
        return Uni.createFrom().voidItem();
    }

    public static String hash(String password) {
        try {
            var salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            var key = derive(password, salt, 210_000);
            return "pbkdf2$" + 210_000 + "$" + hex(salt) + "$" + hex(key);
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    public static boolean matches(String password, String stored) {
        if (stored == null || password == null) return false;
        var parts = stored.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) return false;
        try {
            var iterations = Integer.parseInt(parts[1]);
            var salt = unhex(parts[2]);
            var expected = unhex(parts[3]);
            var actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) throws Exception {
        var spec = new javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }

    private static String hex(byte[] bytes) {
        var sb = new StringBuilder(bytes.length * 2);
        for (var b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] unhex(String value) {
        var bytes = new byte[value.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}