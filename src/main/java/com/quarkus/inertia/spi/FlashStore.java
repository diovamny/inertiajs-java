package com.quarkus.inertia.spi;

import java.util.Map;

public interface FlashStore {
    void put(String key, Object value);
    void putAll(Map<String, Object> values);
    Map<String, Object> drain();
    boolean hasData();
}
