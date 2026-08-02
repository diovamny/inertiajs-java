package com.quarkus.inertia.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.RawJson;
import com.quarkus.inertia.spi.ErrorMapper;

public interface Inertia {

    Uni<Object> render(String component, Map<String, Object> props);

    Uni<Object> render(String component);

    Uni<Object> render(Enum<?> component);

    Uni<Object> render(Enum<?> component, Map<String, Object> props);

    Uni<Object> redirect(String url);

    Uni<Object> redirect(String url, boolean fullPage);

    Uni<Object> location(String url);

    Uni<Object> back();

    Uni<Object> back(String fallback);

    Uni<Object> back(int status, String fallback);

    void share(String key, Object value);

    void share(Map<String, Object> values);

    RawJson rawJson(String json);

    void always(String key, Object value);

    void flash(String key, Object value);

    void flash(Map<String, Object> values);

    Object getFlash(String key, Object defaultValue);

    Object pullFlash(String key, Object defaultValue);

    void deferred(String group, String name, Supplier<Uni<Object>> resolver);

    void deferred(String name, Supplier<Uni<Object>> resolver);

    void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey);

    void deferred(String group, String name, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl);

    void optional(String key, Supplier<Uni<Object>> resolver);

    void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey);

    void optional(String key, Supplier<Uni<Object>> resolver, String cacheKey, Duration cacheTtl);

    void cache(String key, Supplier<Uni<Object>> resolver);

    void cache(String key, Duration ttl, Supplier<Uni<Object>> resolver);

    void once(String key, Object value);

    void once(String key, Object value, String customKey);

    void once(String key, Supplier<Uni<Object>> resolver);

    void once(String key, Supplier<Uni<Object>> resolver, String customKey);

    void once(String key, Supplier<Uni<Object>> resolver, String customKey, Instant expiresAt);

    void shareOnce(String key, Object value);

    void shareOnce(String key, Supplier<Uni<Object>> resolver);

    void merge(String key, Object value);

    void merge(String key, Object value, boolean deep);

    void merge(String key, Object value, boolean deep, String... matchOn);

    void prepend(String key, Object value);

    void scroll(String key, Map<String, Object> metadata);

    void scroll(String key, Object value, Map<String, Object> metadata);

    void scroll(String key, Object value, Map<String, Object> metadata, String wrapper);

    void rescue(String key);

    void handleErrorUsing(ErrorMapper mapper);

    void meta(String key, Object value);

    void meta(Map<String, Object> values);

    void encryptHistory(boolean encrypt);

    void clearHistory(boolean clear);

    void preserveFragment(boolean preserve);

    void setRootView(String name);

    void withoutSsr(String... paths);

    void disableSsr();

    Map<String, Object> getShared();

    Object getShared(String key, Object defaultValue);

    void flushShared();

    String getVersion();

    void version(String version);
}
