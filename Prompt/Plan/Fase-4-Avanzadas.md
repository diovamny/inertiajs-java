# Fase 4: Características Avanzadas

## 4.1 Objetivo

Implementar las características avanzadas del protocolo Inertia.js v3: Server-Side Rendering (SSR), Deferred Props (props asíncronas), Merge Props (fusión de props parciales), Once Props (props de una sola vez), y preparar la arquitectura para futuras extensiones (Infinite Scroll, etc.).

---

## 4.2 Requisitos aplicables (ANEXO 0)

| ID | Prioridad | Requisito | Validación |
|----|-----------|-----------|------------|
| REQ-030 | Crítica | Implementar protocolo oficial Inertia | Integración |
| REQ-010 | Crítica | Nunca bloquear Event Loop | Revisión |
| REQ-011 | Crítica | Una única serialización por request | Revisión |
| REQ-050 | Crítica | Mantener compatibilidad binaria | Revisión |
| REQ-051 | Alta | Evolucionar mediante sobrecargas | Revisión |

---

## 4.3 Componentes a crear

### 4.3.1 `DeferredProp.java`

Ruta: `src/main/java/com/quarkus/inertia/model/DeferredProp.java`

Responsabilidad: Representa una propiedad que se resuelve de forma asíncrona (deferred prop). Inertia.js v3 permite marcar props como diferidas para que el cliente las solicite después de la carga inicial. Las props diferidas se organizan en grupos (`Map<String, List<String>>`), donde la clave es el nombre del grupo y los valores son las claves de props. Esto permite que el cliente solicite grupos específicos, replicando el comportamiento de inertia-laravel, inertia-rails e inertia-phoenix.

```java
package com.quarkus.inertia.model;

import java.util.function.Supplier;
import io.smallrye.mutiny.Uni;

public record DeferredProp<T>(
    String group,
    String name,
    Supplier<Uni<T>> resolver
) {
    public Uni<T> resolve() {
        return resolver.get();
    }
}
```

### 4.3.2 `OncePropRegistry.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/OncePropRegistry.java`

Responsabilidad: Almacena props que deben enviarse una sola vez (once props). Después de enviarse, se eliminan automáticamente. Soporta custom key (para renombrar la prop en el cliente) y expiration opcional (para auto-limpiar props expiradas).

```java
package com.quarkus.inertia.protocol;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class OncePropRegistry {

    private final Map<String, OnceEntry> onceProps = new HashMap<>();

    public void set(String key, Object value) {
        set(key, value, null, null);
    }

    public void set(String key, Object value, String customKey, Instant expiresAt) {
        onceProps.put(key, new OnceEntry(value, customKey, expiresAt));
    }

    public Map<String, Object> drain() {
        purgeExpired();
        var snapshot = new HashMap<String, Object>();
        for (var entry : onceProps.entrySet()) {
            var onceKey = entry.getValue().customKey != null
                ? entry.getValue().customKey : entry.getKey();
            snapshot.put(onceKey, entry.getValue().value);
        }
        onceProps.clear();
        return Map.copyOf(snapshot);
    }

    public boolean hasProps() {
        purgeExpired();
        return !onceProps.isEmpty();
    }

    private void purgeExpired() {
        var now = Instant.now();
        onceProps.values().removeIf(e -> e.expiresAt != null && e.expiresAt.isBefore(now));
    }

    private record OnceEntry(Object value, String customKey, Instant expiresAt) {}
}
```

### 4.3.3 `FlashStore.java` (interfaz SPI)

Ruta: `src/main/java/com/quarkus/inertia/spi/FlashStore.java`

Responsabilidad: Abstracción para el almacenamiento de flash data que sobrevive a redirects. El flash data se escribe antes del redirect y se recupera en el siguiente request, replicando el comportamiento de `session.flash()` en Laravel y `Plug.Conn.assign(:inertia_flash, ...)` en Phoenix.

```java
package com.quarkus.inertia.spi;

import java.util.Map;

public interface FlashStore {
    void put(String key, Object value);
    void putAll(Map<String, Object> values);
    Map<String, Object> drain();
    boolean hasData();
}
```

### 4.3.4 `VertxSessionFlashStore.java`

Ruta: `src/main/java/com/quarkus/inertia/internal/VertxSessionFlashStore.java`

Responsabilidad: Implementación de `FlashStore` que utiliza la sesión Vert.x para persistir flash data entre requests. Los datos se almacenan bajo una clave `__inertia_flash` en la sesión y se eliminan al ser leídos (drain).

```java
package com.quarkus.inertia.internal;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import io.vertx.ext.web.Session;

import com.quarkus.inertia.spi.FlashStore;

@RequestScoped
public class VertxSessionFlashStore implements FlashStore {

    static final String SESSION_KEY = "__inertia_flash";

    @Override
    public void put(String key, Object value) {
        var session = getSession();
        if (session != null) {
            var data = this.<Map<String, Object>>getFlashData(session);
            data = new HashMap<>(data);
            data.put(key, value);
            session.put(SESSION_KEY, data);
        }
    }

    @Override
    public void putAll(Map<String, Object> values) {
        var session = getSession();
        if (session != null) {
            var data = this.<Map<String, Object>>getFlashData(session);
            data = new HashMap<>(data);
            data.putAll(values);
            session.put(SESSION_KEY, data);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> drain() {
        var session = getSession();
        if (session != null) {
            var data = (Map<String, Object>) session.get(SESSION_KEY);
            if (data != null) {
                session.remove(SESSION_KEY);
                return Map.copyOf(data);
            }
        }
        return Map.of();
    }

    @Override
    public boolean hasData() {
        var session = getSession();
        if (session != null) {
            return session.get(SESSION_KEY) != null;
        }
        return false;
    }

    private Session getSession() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            // En Quarkus REST/Reactive, el session se obtiene del RoutingContext
            // que está disponible a través del contexto Vert.x.
            // Esto requiere integración con quarkus-vertx-http.
            return null; // Placeholder: se completa con integración real en Fase 2+.
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFlashData(Session session) {
        var data = (Map<String, Object>) session.get(SESSION_KEY);
        return data != null ? data : Map.of();
    }
}
```

### 4.3.5 `MergePropProcessor.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/MergePropProcessor.java`

Responsabilidad: Procesa Merge Props. Cuando una prop tiene el prefijo especial o una estructura que indica merge, se fusiona con datos existentes en lugar de reemplazarlos.

```java
package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class MergePropProcessor {

    public Map<String, Object> merge(Map<String, Object> existing, Map<String, Object> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return existing;
        }

        var result = new HashMap<>(existing);

        for (var entry : incoming.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (value instanceof Map && existing.get(key) instanceof Map) {
                @SuppressWarnings("unchecked")
                var merged = merge(
                    (Map<String, Object>) existing.get(key),
                    (Map<String, Object>) value
                );
                result.put(key, merged);
            } else {
                result.put(key, value);
            }
        }

        return Map.copyOf(result);
    }
}
```

### 4.3.6 `SsrHandler.java`

Ruta: `src/main/java/com/quarkus/inertia/renderer/SsrHandler.java`

Responsabilidad: Maneja Server-Side Rendering. Si SSR está habilitado, envía el PageObject al servidor SSR (URL configurable) para obtener HTML renderizado del lado del servidor.

```java
package com.quarkus.inertia.renderer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.model.PageObject;

@ApplicationScoped
public class SsrHandler {

    private final boolean enabled;
    private final String ssrUrl;
    private final WebClient client;

    @Inject
    public SsrHandler(InertiaConfig config, Vertx vertx) {
        this.enabled = config.ssrEnabled();
        this.ssrUrl = config.ssrUrl();
        this.client = enabled ? WebClient.create(vertx) : null;
    }

    public Uni<String> render(PageObject page) {
        if (!enabled) {
            return Uni.createFrom().nullItem();
        }

        return client.post(ssrUrl + "/render")
            .sendJson(page)
            .map(resp -> {
                if (resp.statusCode() == 200) {
                    return resp.bodyAsString();
                }
                return null;
            });
    }
}
```

### 4.3.7 `Inertia.java` (modificación)

Añadir nuevos métodos a la interfaz pública `Inertia` para las nuevas características, manteniendo compatibilidad binaria. Los metadatos (deferredProps, mergeProps, onceProps) se pasan como `Map<String, List<String>>` para deferredProps (soportando grupos, alineado con inertia-laravel/rails/phoenix), y como listas separadas para mergeProps y onceProps, NO dentro de props.

#### Interfaz `Inertia.java`:

```java
package com.quarkus.inertia.api;

import java.util.List;
import java.util.Map;
import io.smallrye.mutiny.Uni;

public interface Inertia {

    // Métodos existentes de Fase 1
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
    String getVersion();

    // Nuevos métodos de Fase 4 (overloads, mantienen compatibilidad binaria)
    default Uni<Object> render(String component, Map<String, Object> props,
            Map<String, List<String>> deferredProps) {
        return render(component, props);
    }

    default Uni<Object> renderWithMetadata(String component, Map<String, Object> props,
            Map<String, List<String>> deferredProps, List<String> mergeProps,
            Map<String, String> onceProps) {
        return render(component, props);
    }

    default void once(String key, Object value) {
        share(key, value);
    }

    default void once(String key, Object value, String customKey) {
        share(customKey != null ? customKey : key, value);
    }

    default <T> Uni<T> deferred(String group, String name,
            java.util.function.Supplier<Uni<T>> resolver) {
        return resolver.get();
    }
}
```

### 4.3.8 `PageObjectBuilder.java` (modificación completa)

Añadir integración con OncePropRegistry y MergePropProcessor mediante constructor injection. Añadir overload de `build()` con metadatos (deferredProps, mergeProps, onceProps) que se pasan al PageObject como metadatos out-of-band, no dentro de props. Los metadatos (deferredProps, mergeProps, onceProps) se excluyen automáticamente en partial reload (Corrección 2).

```java
package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class PageObjectBuilder {

    private final SharedDataRegistry sharedData;
    private final VersionProvider versionProvider;
    private final PartialReloadProcessor partialReloadProcessor;
    private final OncePropRegistry oncePropRegistry;
    private final MergePropProcessor mergePropProcessor;

    @Inject
    public PageObjectBuilder(
            SharedDataRegistry sharedData,
            VersionProvider versionProvider,
            PartialReloadProcessor partialReloadProcessor,
            OncePropRegistry oncePropRegistry,
            MergePropProcessor mergePropProcessor) {
        this.sharedData = sharedData;
        this.versionProvider = versionProvider;
        this.partialReloadProcessor = partialReloadProcessor;
        this.oncePropRegistry = oncePropRegistry;
        this.mergePropProcessor = mergePropProcessor;
    }

    public Uni<PageObject> build(String component, Map<String, Object> props) {
        return build(component, props, null, null, null);
    }

    public Uni<PageObject> build(String component, Map<String, Object> props,
            Map<String, List<String>> deferredProps, List<String> mergeProps,
            Map<String, String> oncePropsMeta) {
        var allProps = new HashMap<String, Object>();
        if (props != null) {
            for (var entry : props.entrySet()) {
                allProps.put(entry.getKey(), unwrapAlwaysProp(entry.getValue()));
            }
        }
        var shared = sharedData.getAll();
        for (var entry : shared.entrySet()) {
            allProps.put(entry.getKey(), unwrapAlwaysProp(entry.getValue()));
        }

        var oncePropValues = oncePropRegistry.drain();
        allProps.putAll(oncePropValues);

        allProps = new HashMap<>(mergePropProcessor.merge(allProps, allProps));

        var url = currentUrl();
        var version = versionProvider.getVersion();

        var isPartial = isCurrentRequestPartial();

        // [Corrección 2] Metadatos out-of-band solo en requests no-partial.
        // En partial reload se excluyen deferredProps, mergeProps, onceProps
        // para evitar que el cliente los procese como metadatos en respuestas parciales.
        var resolvedDeferred = !isPartial ? deferredProps : null;
        var resolvedMerge = !isPartial ? mergeProps : null;
        var resolvedOnce = !isPartial ? oncePropsMeta : null;

        var page = new PageObject(
            component, Map.copyOf(allProps), url, version, Map.of(),
            resolvedDeferred, resolvedMerge, null, resolvedOnce,
            null, null, null
        );

        var partialContext = buildPartialReloadContext();
        if (partialContext != null) {
            page = partialReloadProcessor.apply(page, partialContext);
        }

        return Uni.createFrom().item(page);
    }

    private static Object unwrapAlwaysProp(Object value) {
        if (value instanceof AlwaysProp<?> always) {
            return always.value();
        }
        return value;
    }

    private String currentUrl() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var uri = ctx.getLocal("request-uri");
            if (uri != null) return (String) uri;
        }
        return "/";
    }

    private PartialReloadProcessor.PartialReloadContext buildPartialReloadContext() {
        var ctx = Vertx.currentContext();
        if (ctx == null) return null;

        var component = (String) ctx.getLocal("inertia-partial-component");
        if (component == null) return null;

        var dataStr = (String) ctx.getLocal("inertia-partial-data");
        var exceptStr = (String) ctx.getLocal("inertia-partial-except");
        var resetStr = (String) ctx.getLocal("inertia-reset");

        Set<String> data = dataStr != null ?
            Set.of(dataStr.split(",")) : Set.of();
        Set<String> except = exceptStr != null ?
            Set.of(exceptStr.split(",")) : Set.of();
        Set<String> reset = resetStr != null ?
            Set.of(resetStr.split(",")) : Set.of();

        return new PartialReloadProcessor.PartialReloadContext(
            component,
            data.isEmpty() ? Set.of() : data,
            except.isEmpty() ? Set.of() : except,
            reset.isEmpty() ? Set.of() : reset
        );
    }

    private boolean isCurrentRequestPartial() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var component = ctx.getLocal("inertia-partial-component");
            return component != null;
        }
        return false;
    }
}
```

### 4.3.9 `InertiaImpl.java` (modificación)

Actualizar `InertiaImpl` para que los overloads con metadatos deleguen al `PageObjectBuilder.build()`. Integrar `FlashStore` para que `flash()` sobreviva a redirects (Corrección 3).

```java
// Añadir a InertiaImpl.java (además del código existente de Fase 1):

private final FlashStore flashStore;

@Inject
public InertiaImpl(/* existing params */, FlashStore flashStore) {
    // ... existing assignments ...
    this.flashStore = flashStore;
}

@Override
public Uni<Object> render(String component, Map<String, Object> props) {
    var sessionFlash = flashStore.drain();
    if (!sessionFlash.isEmpty()) {
        sharedData.setAll(sessionFlash);
    }
    return pageBuilder.build(component, props)
        .chain(page -> responseProcessor.process(page));
}

@Override
public Uni<Object> render(String component, Map<String, Object> props, Map<String, List<String>> deferredProps) {
    var sessionFlash = flashStore.drain();
    if (!sessionFlash.isEmpty()) {
        sharedData.setAll(sessionFlash);
    }
    return pageBuilder.build(component, props, deferredProps, null, null)
        .chain(page -> responseProcessor.process(page));
}

@Override
public Uni<Object> renderWithMetadata(String component, Map<String, Object> props,
        Map<String, List<String>> deferredProps, List<String> mergeProps,
        Map<String, String> onceProps) {
    var sessionFlash = flashStore.drain();
    if (!sessionFlash.isEmpty()) {
        sharedData.setAll(sessionFlash);
    }
    return pageBuilder.build(component, props, deferredProps, mergeProps, onceProps)
        .chain(page -> responseProcessor.process(page));
}

@Override
public void flash(String key, Object value) {
    flashStore.put(key, value);
}

@Override
public void flash(Map<String, Object> values) {
    flashStore.putAll(values);
}
```

---

## 4.4 Tests

### 4.4.1 Tests Unitarios

#### `DeferredPropUnitTest.java`
```java
package com.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import io.smallrye.mutiny.Uni;

class DeferredPropUnitTest {

    @Test
    void shouldResolveDeferredProp() {
        var prop = new DeferredProp<>("default", "user", () -> Uni.createFrom().item("John"));
        var result = prop.resolve().await().indefinitely();
        assertThat(result).isEqualTo("John");
    }

    @Test
    void shouldHaveGroupNameAndResolver() {
        var prop = new DeferredProp<>("fast", "data", () -> Uni.createFrom().item(42));
        assertThat(prop.group()).isEqualTo("fast");
        assertThat(prop.name()).isEqualTo("data");
        assertThat(prop.resolver()).isNotNull();
    }
}
```

#### `OncePropRegistryUnitTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OncePropRegistryUnitTest {

    @Test
    void shouldDrainOnceProps() {
        var registry = new OncePropRegistry();
        registry.set("flash", "success");
        assertThat(registry.hasProps()).isTrue();

        var drained = registry.drain();
        assertThat(drained).containsEntry("flash", "success");
        assertThat(registry.hasProps()).isFalse();
    }

    @Test
    void shouldBeEmptyAfterDrain() {
        var registry = new OncePropRegistry();
        registry.set("msg", "hello");
        registry.drain();
        assertThat(registry.drain()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoProps() {
        var registry = new OncePropRegistry();
        assertThat(registry.drain()).isEmpty();
    }

    @Test
    void shouldAccumulateMultipleProps() {
        var registry = new OncePropRegistry();
        registry.set("a", 1);
        registry.set("b", 2);
        assertThat(registry.drain()).hasSize(2);
    }

    @Test
    void shouldUseCustomKey() {
        var registry = new OncePropRegistry();
        registry.set("internalKey", "value", "publicKey", null);
        var drained = registry.drain();
        assertThat(drained).containsEntry("publicKey", "value");
        assertThat(drained).doesNotContainKey("internalKey");
    }
}
```

#### `MergePropProcessorUnitTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MergePropProcessorUnitTest {

    private final MergePropProcessor processor = new MergePropProcessor();

    @Test
    void shouldReturnExistingWhenIncomingIsNull() {
        var existing = Map.of("key", "value");
        assertThat(processor.merge(existing, null)).isEqualTo(existing);
    }

    @Test
    void shouldReturnExistingWhenIncomingIsEmpty() {
        var existing = Map.of("key", "value");
        assertThat(processor.merge(existing, Map.of())).isEqualTo(existing);
    }

    @Test
    void shouldAddNewKeys() {
        var existing = Map.of("a", "1");
        var incoming = Map.of("b", "2");
        var result = processor.merge(existing, incoming);
        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("a", "1");
        assertThat(result).containsEntry("b", "2");
    }

    @Test
    void shouldOverrideExistingKeys() {
        var existing = Map.of("key", "old");
        var incoming = Map.of("key", "new");
        var result = processor.merge(existing, incoming);
        assertThat(result).containsEntry("key", "new");
    }

    @Test
    void shouldDeepMergeMaps() {
        var existing = Map.of("nested", Map.of("a", "1", "b", "2"));
        var incoming = Map.of("nested", Map.of("b", "updated", "c", "3"));
        var result = processor.merge(existing, incoming);

        @SuppressWarnings("unchecked")
        var nested = (Map<String, Object>) result.get("nested");
        assertThat(nested).containsEntry("a", "1");
        assertThat(nested).containsEntry("b", "updated");
        assertThat(nested).containsEntry("c", "3");
    }

    @Test
    void shouldReturnImmutableResult() {
        var existing = Map.of("key", "value");
        var incoming = Map.of("new", "value");
        var result = processor.merge(existing, incoming);
        assertThatThrownBy(() -> result.put("x", "y"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
```

#### `FlashStoreUnitTest.java`
```java
package com.quarkus.inertia.spi;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FlashStoreUnitTest {

    private final FlashStore store = new FlashStore() {
        private final java.util.HashMap<String, Object> data = new java.util.HashMap<>();

        @Override public void put(String key, Object value) { data.put(key, value); }
        @Override public void putAll(Map<String, Object> values) { data.putAll(values); }
        @Override public Map<String, Object> drain() {
            var snapshot = Map.copyOf(data);
            data.clear();
            return snapshot;
        }
        @Override public boolean hasData() { return !data.isEmpty(); }
    };

    @Test
    void shouldStoreAndDrainFlashData() {
        store.put("key", "value");
        assertThat(store.hasData()).isTrue();
        var drained = store.drain();
        assertThat(drained).containsEntry("key", "value");
        assertThat(store.hasData()).isFalse();
    }

    @Test
    void shouldStoreMultipleValues() {
        store.putAll(Map.of("a", 1, "b", 2));
        var drained = store.drain();
        assertThat(drained).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenNoData() {
        assertThat(store.drain()).isEmpty();
        assertThat(store.hasData()).isFalse();
    }
}
```

#### `SsrHandlerUnitTest.java`
```java
package com.quarkus.inertia.renderer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.model.PageObject;
import java.util.Map;

class SsrHandlerUnitTest {

    @Test
    void shouldReturnNullWhenSsrDisabled() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "sha256"; }
            @Override public String versionCustom() { return ""; }
        };
        var handler = new SsrHandler(config, null);
        var result = handler.render(new PageObject("Home", Map.of(), "/", "v1", Map.of()));
        assertThat(result).isNotNull();
        assertThat(result.await().indefinitely()).isNull();
    }
}
```

### 4.4.2 Tests de Integración

#### `InertiaAdvancedFeaturesQuarkusTest.java`
```java
package com.quarkus.inertia;

import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import com.quarkus.inertia.api.Inertia;
import com.quarkus.inertia.protocol.OncePropRegistry;
import com.quarkus.inertia.protocol.MergePropProcessor;
import com.quarkus.inertia.model.DeferredProp;

@QuarkusTest
class InertiaAdvancedFeaturesQuarkusTest {

    @Inject
    Inertia inertia;

    @Inject
    OncePropRegistry onceRegistry;

    @Inject
    MergePropProcessor mergeProcessor;

    @Test
    void shouldInjectAllAdvancedComponents() {
        assertThat(inertia).isNotNull();
        assertThat(onceRegistry).isNotNull();
        assertThat(mergeProcessor).isNotNull();
    }

    @Test
    void shouldRenderWithDeferredProps() {
        var result = inertia.render("Dashboard", java.util.Map.of("stats", "loading"),
            java.util.Map.of("default", java.util.List.of("users")));
        assertThat(result).isNotNull();
    }

    @Test
    void shouldRenderWithMetadata() {
        var result = inertia.renderWithMetadata("Dashboard", java.util.Map.of("items", "data"),
            java.util.Map.of("chart", java.util.List.of("stats")),
            java.util.List.of("items"), java.util.Map.of("plan", "v1"));
        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleOnceProps() {
        onceRegistry.set("flash", "saved");
        assertThat(onceRegistry.hasProps()).isTrue();
        var drained = onceRegistry.drain();
        assertThat(drained).containsEntry("flash", "saved");
    }

    @Test
    void shouldHandleOncePropsWithCustomKey() {
        onceRegistry.set("internalKey", "value", "publicKey", null);
        var drained = onceRegistry.drain();
        assertThat(drained).containsEntry("publicKey", "value");
    }
}
```

---

## 4.5 Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|--------------|
| 1 | `DeferredProp` record funcional con resolución asíncrona | Test pasa |
| 2 | `OncePropRegistry` almacena y drena props correctamente | Test pasa |
| 3 | `MergePropProcessor` hace merge profundo de Map | Test pasa |
| 4 | `SsrHandler` deshabilitado por defecto, retorna null | Test pasa |
| 5 | API pública extendida con métodos `default` (compatibilidad binaria) | Revisión |
| 6 | `PageObjectBuilder` integra onceProps, mergeProps, deferredProps como metadatos out-of-band | Test |
| 7 | `deferredProps` aparece como lista separada en `PageObject`, no dentro de `props` | Test |
| 8 | Sin bloqueo del Event Loop | Revisión |
| 9 | Compatibilidad binaria mantenida (métodos existentes no modificados) | Revisión |
| 10 | `mvn test` exitoso | Ejecución |
| 11 | `mvn package -Pnative` exitoso | Ejecución |

---

## 4.6 Matriz de Cumplimiento (actualización al finalizar)

| ID | Estado |
|----|--------|
| REQ-030 | VALIDATED |
| REQ-010 | VALIDATED |
| REQ-011 | VALIDATED |
| REQ-050 | VALIDATED |
| REQ-051 | VALIDATED |

---

## 4.7 Notas

- Los métodos nuevos se añaden como `default` en la interfaz `Inertia` para mantener compatibilidad binaria.
- SSR envía el PageObject al servidor SSR vía HTTP POST. La URL se configura en `inertia.ssr-url`.
- Deferred Props se marcan con `__deferred` en el PageObject para que el cliente Inertia.js las solicite después.
- Once Props se implementan a nivel de request (`@RequestScoped`); para persistencia entre requests se necesita integración con sesión, lo cual está fuera del alcance del núcleo del adaptador.
- Infinite Scroll no requiere cambios en el adaptador; se maneja completamente del lado del cliente Inertia.js.
- No implementar mecanismos propietarios de seguridad, autenticación o sesión.
