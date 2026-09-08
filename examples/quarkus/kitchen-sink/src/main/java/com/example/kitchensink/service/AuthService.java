package com.example.kitchensink.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import com.example.kitchensink.entity.User;
import com.example.kitchensink.repository.UserRepository;

/**
 * Session-based authentication for the demo. The Vert.x session cookie keeps
 * the logged-in user id; passwords are hashed with PBKDF2-HMAC-SHA256.
 */
@RequestScoped
public class AuthService {

    static final String SESSION_USER_KEY = "kitchenSink.userId";

    private static final int PBKDF2_ITERATIONS = 210_000;
    private static final int PBKDF2_KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    @Inject
    Instance<RoutingContext> routingContext;

    @Inject
    UserRepository userRepository;

    private User cached;
    private boolean cachedSet;

    public User currentUser() {
        if (cachedSet) {
            return cached;
        }
        cachedSet = true;
        var rc = resolve();
        var session = rc != null ? rc.session() : null;
        var id = session != null ? session.get(SESSION_USER_KEY) : null;
        if (!(id instanceof Long userId)) {
            cached = null;
            return null;
        }
        cached = userRepository.findById(userId);
        return cached;
    }

    public boolean isAuthenticated() {
        return currentUser() != null;
    }

    public void login(User user) {
        var session = requireSession();
        session.put(SESSION_USER_KEY, user.id);
        cached = user;
        cachedSet = true;
    }

    public void logout() {
        var rc = resolve();
        var session = rc != null ? rc.session() : null;
        if (session != null) {
            session.remove(SESSION_USER_KEY);
        }
        cached = null;
        cachedSet = true;
    }

    /** Shared prop payload for {@code inertia.share("auth", ...)}. */
    public Map<String, Object> authProps() {
        var payload = new LinkedHashMap<String, Object>();
        var user = currentUser();
        if (user == null) {
            payload.put("user", null);
            return payload;
        }
        var userMap = new LinkedHashMap<String, Object>();
        userMap.put("id", user.id);
        userMap.put("name", user.name);
        userMap.put("email", user.email);
        payload.put("user", userMap);
        return payload;
    }

    private RoutingContext resolve() {
        try {
            return routingContext.get();
        } catch (Exception e) {
            return null;
        }
    }

    private io.vertx.ext.web.Session requireSession() {
        var rc = resolve();
        var session = rc != null ? rc.session() : null;
        if (session == null) {
            throw new IllegalStateException("No active session");
        }
        return session;
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
