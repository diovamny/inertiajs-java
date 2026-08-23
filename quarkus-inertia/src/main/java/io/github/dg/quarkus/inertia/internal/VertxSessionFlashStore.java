package io.github.dg.quarkus.inertia.internal;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import io.vertx.ext.web.RoutingContext;

import io.github.dg.quarkus.inertia.spi.FlashStore;

/**
 * Default {@link FlashStore} implementation storing flash data inside the
 * Vert.x session under a reserved key; data survives the redirect and is
 * drained when the next page is rendered. Application-scoped, using the
 * current routing context for per-request session access.
 */
@ApplicationScoped
public class VertxSessionFlashStore implements FlashStore {

    static final String SESSION_KEY = "__inertia_flash";

    @Inject
    Instance<RoutingContext> routingContext;

    @Override
    public void put(String key, Object value) {
        var session = getSession();
        if (session == null) return;
        var data = getFlashData();
        var mutable = new HashMap<>(data);
        mutable.put(key, value);
        session.put(SESSION_KEY, mutable);
    }

    @Override
    public void putAll(Map<String, Object> values) {
        if (values == null || values.isEmpty()) return;
        var session = getSession();
        if (session == null) return;
        var data = getFlashData();
        var mutable = new HashMap<>(data);
        mutable.putAll(values);
        session.put(SESSION_KEY, mutable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> drain() {
        var session = getSession();
        if (session == null) return Map.of();

        var raw = session.get(SESSION_KEY);
        if (raw instanceof Map && !((Map<?, ?>) raw).isEmpty()) {
            session.remove(SESSION_KEY);
            return Map.copyOf((Map<String, Object>) raw);
        }

        return Map.of();
    }

    @Override
    public Object get(String key, Object defaultValue) {
        var data = getFlashData();
        return data.containsKey(key) ? data.get(key) : defaultValue;
    }

    @Override
    public Object pull(String key, Object defaultValue) {
        var session = getSession();
        if (session == null) return defaultValue;
        var data = getFlashData();
        if (!data.containsKey(key)) return defaultValue;
        var value = data.get(key);
        var mutable = new HashMap<>(data);
        mutable.remove(key);
        session.put(SESSION_KEY, mutable);
        return value;
    }

    @Override
    public boolean hasData() {
        var session = getSession();
        if (session == null) return false;
        var raw = session.get(SESSION_KEY);
        return raw instanceof Map && !((Map<?, ?>) raw).isEmpty();
    }

    private io.vertx.ext.web.Session getSession() {
        try {
            var rc = routingContext.get();
            return rc != null ? rc.session() : null;
        } catch (Exception ignored) {
            // no active request / no routing context: fall through
        }
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var rc = ctx.getLocal("inertia-routing-context");
            if (rc instanceof io.vertx.ext.web.RoutingContext r) {
                return r.session();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFlashData() {
        var session = getSession();
        if (session != null) {
            var raw = session.get(SESSION_KEY);
            if (raw instanceof Map) {
                return (Map<String, Object>) raw;
            }
        }
        return Map.of();
    }
}