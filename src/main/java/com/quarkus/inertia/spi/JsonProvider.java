package com.quarkus.inertia.spi;

public interface JsonProvider {
    String toJson(Object value);
    <T> T fromJson(String json, Class<T> type);
}
