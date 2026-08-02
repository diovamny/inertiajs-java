package com.quarkus.inertia.api;

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

    void share(String key, Object value);

    void share(Map<String, Object> values);

    void always(String key, Object value);

    void flash(String key, Object value);

    void flash(Map<String, Object> values);

    void deferred(String group, String name, Supplier<Uni<Object>> resolver);

    void once(String key, Object value);

    void merge(String key, Object value);

    <T> void once(String key, T value, String customKey);

    <T> void merge(String key, T value, boolean deep);

    void encryptHistory(boolean encrypt);

    void clearHistory(boolean clear);

    void preserveFragment(boolean preserve);

    String getVersion();
}
