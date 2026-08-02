package com.quarkus.inertia.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.AlwaysProp;

public interface Inertia {

    Uni<Object> render(String component, Map<String, Object> props);

    Uni<Object> render(String component);

    Uni<Object> redirect(String url);

    Uni<Object> location(String url);

    Uni<Object> back();

    Uni<Object> back(String fallback);

    Uni<Object> back(int status, String fallback);

    void share(String key, Object value);

    void share(Map<String, Object> values);

    void always(String key, Object value);

    void flash(String key, Object value);

    void flash(Map<String, Object> values);

    void deferred(String group, String name, Supplier<Uni<Object>> resolver);

    void deferred(String name, Supplier<Uni<Object>> resolver);

    void optional(String key, Supplier<Uni<Object>> resolver);

    void once(String key, Object value);

    void once(String key, Object value, String customKey);

    void merge(String key, Object value);

    void merge(String key, Object value, boolean deep);

    void prepend(String key, Object value);

    void scroll(String key, Map<String, Object> metadata);

    void rescue(String key);

    void meta(String key, Object value);

    void meta(Map<String, Object> values);

    void encryptHistory(boolean encrypt);

    void clearHistory(boolean clear);

    void preserveFragment(boolean preserve);

    void setRootView(String name);

    void withoutSsr(String... paths);

    void disableSsr();

    Map<String, Object> getShared();

    void flushShared();

    String getVersion();

    void version(String version);
}
