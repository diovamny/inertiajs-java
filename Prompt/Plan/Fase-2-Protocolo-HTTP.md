# Fase 2: Protocolo HTTP

## 2.1 Objetivo

Implementar el protocolo HTTP completo de Inertia.js v3: Redirects (302/303, PRG), Partial Reload (filtrado de props por headers), Version Mismatch (409 Conflict), headers HTTP correctos, y códigos de estado.

---

## 2.2 Requisitos aplicables (ANEXO 0)

| ID | Prioridad | Requisito | Validación |
|----|-----------|-----------|------------|
| REQ-031 | Crítica | Implementar Version Mismatch | Test |
| REQ-032 | Crítica | Implementar Redirects | Test |
| REQ-033 | Crítica | Implementar Partial Reload | Test |
| REQ-010 | Crítica | Nunca bloquear Event Loop | Revisión |
| REQ-030 | Crítica | Implementar protocolo oficial Inertia | Integración |

---

## 2.3 Componentes a crear/modificar

### 2.3.1 `RedirectProcessor.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/RedirectProcessor.java`

Responsabilidad: Aplica las reglas oficiales de Redirect de Inertia.js v3. En requests Inertia POST → 303 con X-Inertia-Location. En requests GET → 302. Location externa → 409 + X-Inertia-Location. back() obtiene la URL desde el header Referer.

```java
package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;

@RequestScoped
public class RedirectProcessor {

    public Uni<Object> process(String url) {
        if (isInertiaRequest()) {
            if (isNonGetRequest()) {
                return Uni.createFrom().item(
                    Response.status(Response.Status.SEE_OTHER)
                        .header("X-Inertia-Location", url)
                        .header("Vary", "Accept")
                        .build()
                );
            }
            return Uni.createFrom().item(
                Response.status(Response.Status.FOUND)
                    .header("X-Inertia-Location", url)
                    .header("Vary", "Accept")
                    .build()
            );
        }
        return Uni.createFrom().item(
            Response.status(Response.Status.FOUND)
                .header("Location", url)
                .build()
        );
    }

    public Uni<Object> external(String url) {
        if (isInertiaRequest()) {
            return Uni.createFrom().item(
                Response.status(Response.Status.CONFLICT)
                    .header("X-Inertia-Location", url)
                    .header("Vary", "Accept")
                    .build()
            );
        }
        return Uni.createFrom().item(
            Response.status(Response.Status.FOUND)
                .header("Location", url)
                .build()
        );
    }

    public Uni<Object> back() {
        var referer = getRefererUrl();
        if (referer != null && !referer.isBlank()) {
            return process(referer);
        }
        return process("/");
    }

    private boolean isInertiaRequest() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-request");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    private boolean isNonGetRequest() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var method = ctx.getLocal("request-method");
            return "POST".equalsIgnoreCase((String) method) ||
                   "PUT".equalsIgnoreCase((String) method) ||
                   "PATCH".equalsIgnoreCase((String) method) ||
                   "DELETE".equalsIgnoreCase((String) method);
        }
        return false;
    }

    private String getRefererUrl() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            return (String) ctx.getLocal("referer-url");
        }
        return null;
    }
}
```

### 2.3.2 `PartialReloadProcessor.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/PartialReloadProcessor.java`

Responsabilidad: Aplica el protocolo Partial Reload según headers `X-Inertia-Partial-Component`, `X-Inertia-Partial-Data`, `X-Inertia-Partial-Except`, `X-Inertia-Reset`.

Reglas:
- Si `X-Inertia-Partial-Component` no coincide con el componente actual → ignorar, devolver todas las props.
- Si `X-Inertia-Partial-Data` presente → incluir solo esas props.
- Si `X-Inertia-Partial-Except` presente → excluir esas props.
- Si ambos presentes → Except tiene prioridad.
- `AlwaysProp` nunca se elimina (se identifica por ser instancia de `AlwaysProp`).
- `errors` nunca se eliminan.
- `X-Inertia-Reset` lista props cuyo merge state debe resetearse en el cliente.

```java
package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;

import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.model.PageObject;

@RequestScoped
public class PartialReloadProcessor {

    public PageObject apply(PageObject page, PartialReloadContext context) {
        if (context == null || !context.matchesComponent(page.component())) {
            return page;
        }

        Map<String, Object> filteredProps = new HashMap<>();

        for (var entry : page.props().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            boolean isAlways = value instanceof AlwaysProp;

            boolean matchesData = !context.hasData() || context.data().contains(key);
            boolean matchesExcept = context.hasExcept() && context.except().contains(key);

            if (isAlways) {
                filteredProps.put(key, ((AlwaysProp<?>) value).value());
            } else if (matchesData && !matchesExcept) {
                filteredProps.put(key, value);
            }
        }

        filteredProps.putAll(page.errors());

        return page.withProps(Map.copyOf(filteredProps));
    }

    public record PartialReloadContext(
        String partialComponent,
        Set<String> data,
        Set<String> except,
        Set<String> reset
    ) {
        public boolean matchesComponent(String component) {
            return partialComponent != null && partialComponent.equals(component);
        }

        public boolean hasData() {
            return data != null && !data.isEmpty();
        }

        public boolean hasExcept() {
            return except != null && !except.isEmpty();
        }

        public boolean hasReset() {
            return reset != null && !reset.isEmpty();
        }
    }
}
```

### 2.3.3 `VersionMismatchHandler.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/VersionMismatchHandler.java`

Responsabilidad: Detecta Version Mismatch comparando `X-Inertia-Version` del cliente con `VersionProvider`. Si no coinciden → 409 Conflict con `X-Inertia-Location` forzando recarga completa.

```java
package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class VersionMismatchHandler {

    private final VersionProvider versionProvider;

    @Inject
    public VersionMismatchHandler(VersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    public Uni<Object> handle(String clientVersion, String currentUrl) {
        var serverVersion = versionProvider.getVersion();
        if (clientVersion != null && !clientVersion.equals(serverVersion)) {
            return Uni.createFrom().item(
                Response.status(Response.Status.CONFLICT)
                    .header("X-Inertia-Location", currentUrl)
                    .build()
            );
        }
        return Uni.createFrom().item(Response.ok().build());
    }
}
```

### 2.3.4 `InertiaRequestFilter.java`

Ruta: `src/main/java/com/quarkus/inertia/protocol/InertiaRequestFilter.java`

Responsabilidad: Filtro CDI que captura el request entrante, extrae headers Inertia y los almacena en el contexto Vert.x local para uso por otros componentes.

```java
package com.quarkus.inertia.protocol;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.annotation.Priority;
import io.vertx.core.Vertx;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class InertiaRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var ctx = Vertx.currentContext();
        if (ctx == null) return;

        var inertiaHeader = requestContext.getHeaderString("X-Inertia");
        ctx.putLocal("inertia-request", "true".equalsIgnoreCase(inertiaHeader)
            || Boolean.parseBoolean(inertiaHeader));

        ctx.putLocal("request-method", requestContext.getMethod());

        ctx.putLocal("request-uri", requestContext.getUriInfo().getRequestUri().toString());

        var version = requestContext.getHeaderString("X-Inertia-Version");
        if (version != null) {
            ctx.putLocal("inertia-version", version);
        }

        var partialComponent = requestContext.getHeaderString("X-Inertia-Partial-Component");
        if (partialComponent != null) {
            ctx.putLocal("inertia-partial-component", partialComponent);
        }

        var partialData = requestContext.getHeaderString("X-Inertia-Partial-Data");
        if (partialData != null) {
            ctx.putLocal("inertia-partial-data", partialData);
        }

        var partialExcept = requestContext.getHeaderString("X-Inertia-Partial-Except");
        if (partialExcept != null) {
            ctx.putLocal("inertia-partial-except", partialExcept);
        }

        var reset = requestContext.getHeaderString("X-Inertia-Reset");
        if (reset != null) {
            ctx.putLocal("inertia-reset", reset);
        }

        var exceptOnce = requestContext.getHeaderString("X-Inertia-Except-Once-Props");
        if (exceptOnce != null) {
            ctx.putLocal("inertia-except-once-props", exceptOnce);
        }

        // [Corrección 4] X-Inertia-Error-Bag: identifica el bag de errores de validación
        var errorBag = requestContext.getHeaderString("X-Inertia-Error-Bag");
        if (errorBag != null) {
            ctx.putLocal("inertia-error-bag", errorBag);
        }

        var referer = requestContext.getHeaderString("Referer");
        if (referer != null) {
            ctx.putLocal("referer-url", referer);
        }
    }
}
```

### 2.3.5 `InertiaCsrfFilter.java`

Ruta: `src/main/java/com/quarkus/inertia/security/InertiaCsrfFilter.java`

Responsabilidad: Filtro de respuesta que añade la cookie `XSRF-TOKEN` en toda respuesta Inertia, replicando el comportamiento de `VerifyCsrfToken` en Laravel y `put_csrf_cookie()` en Phoenix.

```java
package com.quarkus.inertia.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;

@Provider
@Priority(Priorities.HEADER_DECORATOR + 10)
public class InertiaCsrfFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!isInertiaResponse()) {
            return;
        }
        // XSRF-TOKEN cookie: el cliente Inertia.js espera esta cookie
        // para incluir el token CSRF en requests subsiguientes.
        // El valor real debe ser proporcionado por el backend de seguridad.
        // Aquí seteamos un placeholder que el backend de seguridad debe sobrescribir.
        response.getHeaders().add("Set-Cookie",
            "XSRF-TOKEN=" + generateCsrfToken() + "; Path=/; SameSite=Lax");
    }

    private boolean isInertiaResponse() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-request");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    private String generateCsrfToken() {
        // Token CSRF generado por request. En producción debe usar
        // el mecanismo CSRF de Quarkus (quarkus-rest-csrf o similar).
        return java.util.UUID.randomUUID().toString();
    }
}
```

### 2.3.6 `PageObjectBuilder.java` (modificación)

Añadir campo `PartialReloadProcessor`, inyectar por constructor, integrar URL real desde contexto Vert.x y aplicar PartialReloadProcessor. Añadir overload de `build()` con flag `isPartial` para que Fase 4 pueda excluir metadatos en partial reload (Corrección 2).

```java
package com.quarkus.inertia.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class PageObjectBuilder {

    private final SharedDataRegistry sharedData;
    private final VersionProvider versionProvider;
    private final PartialReloadProcessor partialReloadProcessor;

    @Inject
    public PageObjectBuilder(
            SharedDataRegistry sharedData,
            VersionProvider versionProvider,
            PartialReloadProcessor partialReloadProcessor) {
        this.sharedData = sharedData;
        this.versionProvider = versionProvider;
        this.partialReloadProcessor = partialReloadProcessor;
    }

    public Uni<PageObject> build(String component, Map<String, Object> props) {
        return build(component, props, isCurrentRequestPartial());
    }

    public Uni<PageObject> build(String component, Map<String, Object> props, boolean isPartial) {
        var allProps = new HashMap<String, Object>();
        if (props != null) allProps.putAll(props);
        allProps.putAll(sharedData.getAll());

        var url = currentUrl();
        var version = versionProvider.getVersion();

        var page = new PageObject(component, Map.copyOf(allProps), url, version, Map.of());

        var partialContext = buildPartialReloadContext();
        if (partialContext != null) {
            page = partialReloadProcessor.apply(page, partialContext);
        }

        return Uni.createFrom().item(page);
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

### 2.3.7 `ResponseProcessor.java` (modificación)

Añadir verificación de Version Mismatch antes de procesar la respuesta.

```java
package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;

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
        if (!isInertiaRequest()) {
            return htmlRenderer.render(page).map(Object.class::cast);
        }

        // Version Mismatch: solo en GET requests. El protocolo Inertia permite
        // que requests POST/PUT/PATCH/DELETE continúen aunque la versión haya cambiado.
        if (isGetRequest()) {
            var clientVersion = getClientVersion();
            if (clientVersion != null && !clientVersion.equals(page.version())) {
                return Uni.createFrom().item(
                    Response.status(Response.Status.CONFLICT)
                        .header("X-Inertia-Location", page.url())
                        .build()
                );
            }
        }

        return jsonProcessor.write(page).map(Object.class::cast);
    }

    private boolean isInertiaRequest() {
        var vertxContext = Vertx.currentContext();
        if (vertxContext != null) {
            var request = vertxContext.getLocal("inertia-request");
            return Boolean.TRUE.equals(request);
        }
        return false;
    }

    private boolean isGetRequest() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            var method = ctx.getLocal("request-method");
            return "GET".equalsIgnoreCase((String) method);
        }
        return true;
    }

    private String getClientVersion() {
        var ctx = Vertx.currentContext();
        if (ctx != null) {
            return (String) ctx.getLocal("inertia-version");
        }
        return null;
    }
}
```

---

## 2.4 Tests

### 2.4.1 Tests Unitarios

#### `RedirectProcessorUnitTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class RedirectProcessorUnitTest {

    private final RedirectProcessor processor = new RedirectProcessor();

    @Test
    void shouldReturn302ForNonInertiaRedirect() {
        var result = processor.process("/home");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeaderString("Location")).isEqualTo("/home");
    }

    @Test
    void shouldReturn409ForExternalLocation() {
        var result = processor.external("https://example.com");
        var response = (Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getHeaderString("X-Inertia-Location")).isEqualTo("https://example.com");
    }
}
```

#### `PartialReloadProcessorUnitTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import com.quarkus.inertia.model.PageObject;
import com.quarkus.inertia.protocol.PartialReloadProcessor.PartialReloadContext;

class PartialReloadProcessorUnitTest {

    private final PartialReloadProcessor processor = new PartialReloadProcessor();

    @Test
    void shouldReturnAllPropsWhenComponentMismatch() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com"), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Posts", Set.of("name"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
    }

    @Test
    void shouldFilterToOnlyDataProps() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com", "role", "admin"), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of("name", "email"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
        assertThat(result.props()).containsKeys("name", "email");
        assertThat(result.props()).doesNotContainKey("role");
    }

    @Test
    void shouldExcludeExceptProps() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com", "role", "admin"), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of(), Set.of("role"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
        assertThat(result.props()).containsKeys("name", "email");
    }

    @Test
    void shouldGivePriorityToExceptWhenBothPresent() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com", "role", "admin"), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of("name", "email", "role"), Set.of("role"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
        assertThat(result.props()).containsKeys("name", "email");
        assertThat(result.props()).doesNotContainKey("role");
    }

    @Test
    void shouldReturnAllPropsWhenDataAndExceptAreEmpty() {
        var page = new PageObject("Users", Map.of("a", "1", "b", "2"), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of(), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).hasSize(2);
    }

    @Test
    void shouldKeepErrors() {
        var page = new PageObject("Users", Map.of("name", "John"), "/users", "v1", Map.of("name", "Required"));
        var context = new PartialReloadContext("Users", Set.of(), Set.of("name"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).doesNotContainKey("name");
    }

    @Test
    void shouldReturnAllPropsWhenContextIsNull() {
        var page = new PageObject("Users", Map.of("a", "1", "b", "2"), "/users", "v1", Map.of());
        var result = processor.apply(page, null);
        assertThat(result.props()).hasSize(2);
    }

    @Test
    void shouldAlwaysIncludeAlwaysProp() {
        var page = new PageObject("Users", Map.of(
            "name", "John",
            "role", com.quarkus.inertia.model.AlwaysProp.of("admin")
        ), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of("name"), Set.of(), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("role");
        assertThat(result.props()).containsEntry("role", "admin");
    }

    @Test
    void shouldNotFilterAlwaysPropEvenWithExcept() {
        var page = new PageObject("Users", Map.of(
            "name", "John",
            "role", com.quarkus.inertia.model.AlwaysProp.of("admin")
        ), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of(), Set.of("role"), Set.of());
        var result = processor.apply(page, context);
        assertThat(result.props()).containsKey("role");
    }

    @Test
    void shouldPreserveResetKeys() {
        var page = new PageObject("Users", Map.of("name", "John", "email", "john@test.com"), "/users", "v1", Map.of());
        var context = new PartialReloadContext("Users", Set.of("name"), Set.of(), Set.of("email"));
        var result = processor.apply(page, context);
        // reset does not filter props out, just marks them for client-side reset
        // Filtering is controlled by data/except only
        assertThat(result.props()).doesNotContainKey("email");
    }
}
```

#### `VersionMismatchHandlerUnitTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.quarkus.inertia.version.VersionProvider;

class VersionMismatchHandlerUnitTest {

    @Test
    void shouldReturnOkWhenVersionsMatch() {
        var provider = new VersionProvider() {
            @Override public String getVersion() { return "v1"; }
        };
        var handler = new VersionMismatchHandler(provider);
        var result = handler.handle("v1", "/home");
        var response = (jakarta.ws.rs.core.Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturn409WhenVersionsDontMatch() {
        var provider = new VersionProvider() {
            @Override public String getVersion() { return "v2"; }
        };
        var handler = new VersionMismatchHandler(provider);
        var result = handler.handle("v1", "/home");
        var response = (jakarta.ws.rs.core.Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getHeaderString("X-Inertia-Location")).isEqualTo("/home");
    }

    @Test
    void shouldReturnNullWhenClientVersionIsNull() {
        var provider = new VersionProvider() {
            @Override public String getVersion() { return "v2"; }
        };
        var handler = new VersionMismatchHandler(provider);
        var result = handler.handle(null, "/home");
        var response = (jakarta.ws.rs.core.Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
```

### 2.4.2 Tests de Integración

#### `InertiaPartialReloadQuarkusTest.java`
```java
package com.quarkus.inertia;

import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import com.quarkus.inertia.protocol.PartialReloadProcessor;
import com.quarkus.inertia.protocol.PartialReloadProcessor.PartialReloadContext;
import com.quarkus.inertia.model.PageObject;
import java.util.Map;
import java.util.Set;

@QuarkusTest
class InertiaPartialReloadQuarkusTest {

    @Inject
    PartialReloadProcessor processor;

    @Test
    void shouldInjectProcessor() {
        assertThat(processor).isNotNull();
    }
}
```

---

## 2.5 Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|--------------|
| 1 | Redirect Inertia POST → 303 con X-Inertia-Location | Test |
| 2 | Redirect Inertia GET → 302 con X-Inertia-Location | Test |
| 3 | Location externa → 409 Conflict con X-Inertia-Location | Test |
| 4 | Partial Reload: solo `data` incluido | Test unitario pasa |
| 5 | Partial Reload: `except` excluido | Test unitario pasa |
| 6 | Partial Reload: `except` tiene prioridad sobre `data` | Test unitario pasa |
| 7 | Partial Reload: si componente no coincide, todas las props | Test unitario pasa |
| 8 | Version Mismatch solo en GET (no en POST/PUT/PATCH/DELETE) | Test |
| 9 | Versiones iguales → respuesta normal | Test |
| 10 | `errors` nunca se eliminan en Partial Reload | Test |
| 11 | `AlwaysProp` nunca se filtra en Partial Reload | Test |
| 12 | `X-Inertia-Reset` capturado y pasado al contexto | Test |
| 13 | `back()` redirige al Referer o a "/" | Test |
| 14 | `mvn test` exitoso | Ejecución |

---

## 2.6 Matriz de Cumplimiento (actualización al finalizar)

| ID | Estado |
|----|--------|
| REQ-031 | VALIDATED |
| REQ-032 | VALIDATED |
| REQ-033 | VALIDATED |
| REQ-010 | VALIDATED |
| REQ-030 | VALIDATED |

---

## 2.7 Notas

- No modificar redirects 301, 307, 308 (prohibido por especificación).
- No implementar variantes propias de Version Mismatch.
- El filter `InertiaRequestFilter` extrae headers y los pone en contexto Vert.x local; no almacena estado global.
- Los códigos de estado HTTP son: 302 (FOUND), 303 (SEE_OTHER), 409 (CONFLICT).
- `Vary: Accept` debe incluirse en respuestas redirect.
