package com.example.pingcrm.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.AccountRepository;
import com.example.pingcrm.repository.UserRepository;

/**
 * Session-based authentication for the demo. The {@code HttpSession} keeps
 * the logged-in user id; passwords are hashed with PBKDF2-HMAC-SHA256.
 */
@Service
public class AuthService {

    static final String SESSION_USER_KEY = "pingcrm.userId";

    private static final int PBKDF2_ITERATIONS = 210_000;
    private static final int PBKDF2_KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public User currentUser() {
        var session = session();
        var id = session != null ? session.getAttribute(SESSION_USER_KEY) : null;
        if (!(id instanceof Long userId)) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    public Account currentAccount() {
        var user = currentUser();
        if (user == null) return null;
        return accountRepository.findById(user.accountId).orElse(null);
    }

    public Long accountId() {
        var account = currentAccount();
        return account != null ? account.id : null;
    }

    public void login(User user) {
        session().setAttribute(SESSION_USER_KEY, (long) user.id);
    }

    public void logout() {
        var session = session();
        if (session != null) {
            session.removeAttribute(SESSION_USER_KEY);
        }
    }

    /** Shared prop payload for {@code inertia.share("auth", ...)}. */
    public Map<String, Object> authProps() {
        var payload = new LinkedHashMap<String, Object>();
        var user = currentUser();
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
        userMap.put("account", Map.of(
            "id", account != null ? account.id : null,
            "name", account != null ? account.name : null));
        payload.put("user", userMap);
        return payload;
    }

    private HttpSession session() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getSession(true);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Password hashing (PBKDF2-HMAC-SHA256), format: pbkdf2$iter$salt$key
    // ------------------------------------------------------------------

    public static String hash(String password) {
        try {
            var salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            var key = derive(password, salt, PBKDF2_ITERATIONS);
            return "pbkdf2$" + PBKDF2_ITERATIONS + "$" + hex(salt) + "$" + hex(key);
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
        var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PBKDF2_KEY_BITS);
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