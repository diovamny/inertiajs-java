package io.github.dg.spring.inertia.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.dg.spring.inertia.spi.FlashStore;

/**
 * Default {@link FlashStore} backed by the {@code HttpSession}: values
 * written via {@code with(...)} survive the redirect and are drained when
 * the next page is rendered.
 */
public class SpringFlashStore implements FlashStore {

    /** Session attribute holding all pending flash values. */
    public static final String SESSION_KEY = "__inertia_flash";

    @Override
    public void put(String key, Object value) {
        var session = session();
        if (session == null) return;
        var data = values(session);
        data.put(key, value);
        session.setAttribute(SESSION_KEY, data);
    }

    @Override
    public void putAll(Map<String, Object> values) {
        var session = session();
        if (session == null) return;
        var data = values(session);
        data.putAll(values);
        session.setAttribute(SESSION_KEY, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object get(String key, Object defaultValue) {
        var session = session();
        if (session == null) return defaultValue;
        return values(session).getOrDefault(key, defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object pull(String key, Object defaultValue) {
        var session = session();
        if (session == null) return defaultValue;
        var data = values(session);
        var value = data.remove(key);
        session.setAttribute(SESSION_KEY, data);
        return value != null ? value : defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> drain() {
        var session = session();
        if (session == null) return Map.of();
        var data = values(session);
        session.removeAttribute(SESSION_KEY);
        return data;
    }

    @Override
    public boolean hasData() {
        var session = session();
        if (session == null) return false;
        return !values(session).isEmpty();
    }

    private HttpSession session() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getSession(true);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> values(HttpSession session) {
        var stored = session.getAttribute(SESSION_KEY);
        if (stored instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }
}