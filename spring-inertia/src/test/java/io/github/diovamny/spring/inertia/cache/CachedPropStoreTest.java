package io.github.diovamny.spring.inertia.cache;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CachedPropStoreTest {

    @Test
    void computesOnceAndReuses() throws InterruptedException {
        var store = new CachedPropStore();
        var calls = new AtomicInteger();
        Object first = store.compute("key", Duration.ofMinutes(1), () -> {
            calls.incrementAndGet();
            return "value";
        });
        Object second = store.compute("key", Duration.ofMinutes(1), () -> {
            calls.incrementAndGet();
            return "value2";
        });
        assertEquals("value", first);
        assertEquals("value", second);
        assertEquals(1, calls.get());
    }

    @Test
    void expiresAfterTtl() throws InterruptedException {
        var store = new CachedPropStore();
        store.compute("key", Duration.ofMillis(10), () -> "a");
        Thread.sleep(30);
        var value = store.compute("key", Duration.ofMillis(10), () -> "b");
        assertEquals("b", value);
    }

    @Test
    void neverExpiresWithoutTtl() {
        var store = new CachedPropStore();
        store.compute("key", null, () -> "a");
        assertTrue(store.contains("key"));
        assertEquals("a", store.compute("key", null, () -> "b"));
    }

    @Test
    void flushAndEvict() {
        var store = new CachedPropStore();
        store.compute("a", null, () -> 1);
        store.compute("b", null, () -> 2);
        store.evict("a");
        assertFalse(store.contains("a"));
        assertTrue(store.contains("b"));
        store.flush();
        assertFalse(store.contains("b"));
    }
}
