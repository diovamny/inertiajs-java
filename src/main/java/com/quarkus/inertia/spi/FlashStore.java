package com.quarkus.inertia.spi;

import java.util.Map;

public interface FlashStore {
    void put(String key, Object value);
    void putAll(Map<String, Object> values);
    Object get(String key, Object defaultValue);
    Object pull(String key, Object defaultValue);
    Map<String, Object> drain();
    boolean hasData();
}
