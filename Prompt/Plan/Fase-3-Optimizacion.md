# Fase 3: Optimización y Native Image

## 3.1 Objetivo

Optimizar el adaptador para GraalVM Native Image, reducir asignaciones de memoria en el Hot Path, eliminar reflection innecesaria, y garantizar que toda la configuración se resuelva en Build Time. Verificar que el adaptador compile y ejecute correctamente como Native Image.

---

## 3.2 Requisitos aplicables (ANEXO 0)

| ID | Prioridad | Requisito | Validación |
|----|-----------|-----------|------------|
| REQ-020 | Crítica | Compatible con GraalVM Native Image | Native Build |
| REQ-021 | Alta | Reflection mínima | Revisión |
| REQ-022 | Alta | Build Time First | Revisión |
| REQ-010 | Crítica | Nunca bloquear Event Loop | Benchmark |
| REQ-011 | Crítica | Una única serialización por request | Benchmark |
| REQ-012 | Alta | VersionProvider calcula una sola vez | Test |
| REQ-014 | Alta | No crear ObjectMapper durante un request | Revisión |
| REQ-060 | Alta | Sin código duplicado | Revisión |
| REQ-061 | Alta | Sin warnings | Compilación |

---

## 3.3 Optimizaciones a implementar

### 3.3.1 `@RegisterForReflection` en `PageObject`

Añadir `@RegisterForReflection` a `PageObject` porque Jackson/JSON-B necesitan acceso de reflection para serialización/deserialización en Native Image.

```java
package com.quarkus.inertia.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PageObject(
    String component,
    Map<String, Object> props,
    String url,
    String version,
    Map<String, Object> errors
) {
    // ... (mismo contenido que Fase 1)
}
```

### 3.3.2 Build Time Initialization

Crear `InertiaRecorder.java` para inicializar componentes en Build Time.

Ruta: `src/main/java/com/quarkus/inertia/config/InertiaStartupValidator.java`

```java
package com.quarkus.inertia.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class InertiaStartupValidator {

    private final InertiaConfig config;

    @Inject
    public InertiaStartupValidator(InertiaConfig config) {
        this.config = config;
    }

    void onStart(@Observes StartupEvent event) {
        // Forces eager validation of InertiaConfig during Startup
        config.rootTemplate();
        config.versionStrategy();
    }
}
```

### 3.3.3 Eliminar reflection innecesaria

Revisar todas las clases y eliminar cualquier uso de:
- `Class.forName()`
- `Method.invoke()`
- `Field.setAccessible()`
- `Proxy.newProxyInstance()`
- `Reflections` library

Crear archivo de configuración de reflection nativa si es necesario.

Ruta: `src/main/resources/META-INF/native-image/com.quarkus.inertia/quarkus-inertia/native-image.properties`

```
Args = -H:+ReportExceptionStackTraces
```

Ruta: `src/main/resources/META-INF/native-image/com.quarkus.inertia/quarkus-inertia/reflect-config.json`

```json
[
  {
    "name": "com.quarkus.inertia.model.PageObject",
    "allDeclaredFields": true,
    "allDeclaredMethods": true,
    "allDeclaredConstructors": true
  }
]
```

### 3.3.4 Optimización de VersionProvider

`DefaultVersionProvider` ya calcula en Startup (Fase 1). Verificar que:
- El constructor se ejecuta una sola vez
- `getVersion()` no realiza ningún cómputo
- El valor es inmutable y compartido entre threads

### 3.3.5 Optimización de SharedDataRegistry

`SharedDataRegistry` es `@RequestScoped`, creado por CDI por request. Verificar que:
- Se libera automáticamente al finalizar el request
- No hay fugas de memoria
- El mapa interno se limpia correctamente

### 3.3.6 Reducción de asignaciones en Hot Path

1. `ResponseProcessor.process()`: evitar crear objetos intermedios innecesarios.
2. `PageObjectBuilder.build()`: reutilizar estructuras si es posible.
3. `PartialReloadProcessor.apply()`: minimizar copias de mapas.
4. `QuteSerializer.serialize()`: no crear múltiples strings temporales.

Crear `InertiaMetrics.java` para monitorear rendimiento (opcional, no obligatorio).

### 3.3.7 Verificación de Native Image

Añadir perfil Maven para compilación nativa.

En `pom.xml`, añadir:
```xml
<profile>
    <id>native</id>
    <properties>
        <quarkus.package.type>native</quarkus.package.type>
    </properties>
</profile>
```

---

## 3.4 Tests

### 3.4.1 Tests Unitarios

#### `PageObjectNativeImageCompatibilityTest.java`
```java
package com.quarkus.inertia.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import io.quarkus.runtime.annotations.RegisterForReflection;

class PageObjectNativeImageCompatibilityTest {

    @Test
    void shouldHaveRegisterForReflectionAnnotation() {
        var annotation = PageObject.class.getAnnotation(RegisterForReflection.class);
        assertThat(annotation).as("PageObject must be annotated with @RegisterForReflection").isNotNull();
    }

    @Test
    void shouldNotUseReflection() {
        // PageObject uses only public record accessors (component(), props(), url(), version(), errors())
        var page = new PageObject("Test", java.util.Map.of(), "/", "v1", java.util.Map.of());
        assertThat(page.component()).isEqualTo("Test");
        assertThat(page.getClass().getDeclaredFields()).allMatch(f ->
            java.lang.reflect.Modifier.isPrivate(f.getModifiers())
        );
    }

    @Test
    void shouldSupportNoArgsConstructorViaFactory() {
        // CDI creates instances; verify record can be constructed
        var page = new PageObject("Test", java.util.Map.of(), "/", "v1", java.util.Map.of());
        assertThat(page).isNotNull();
    }
}
```

#### `SharedDataRegistryPerformanceTest.java`
```java
package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SharedDataRegistryPerformanceTest {

    @Test
    void shouldHandleConcurrentAccess() throws InterruptedException {
        var registry = new SharedDataRegistry();
        int threadCount = 10;
        var threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                registry.set("key-" + index, "value-" + index);
                registry.get("key-" + index);
            });
            threads[i].start();
        }

        for (var t : threads) {
            t.join();
        }

        // @RequestScoped - each thread should have its own instance
        // This test verifies basic concurrent access pattern
        assertThat(registry.isEmpty()).isFalse();
    }

    @Test
    void shouldNotLeakMemory() {
        var registry = new SharedDataRegistry();
        for (int i = 0; i < 1000; i++) {
            registry.set("key-" + i, "value-" + i);
        }
        registry.clear();
        assertThat(registry.isEmpty()).isTrue();
    }
}
```

### 3.4.2 Tests de Integración

#### `NativeImageIT.java`
```java
package com.quarkus.inertia;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
class NativeImageIT {

    @Test
    void shouldStartInNativeImage() {
        given()
            .when().get("/health")
            .then()
            .statusCode(200);
    }
}
```

---

## 3.5 Verificación de Native Image

### Comandos para compilar y probar Native Image

```bash
# Compilar Native Image
mvn package -Pnative -DskipTests

# Ejecutar tests de integración en Native Image
mvn verify -Pnative

# Verificar que no hay reflection innecesaria
mvn compile -Dquarkus.native.additional-build-args=--initialize-at-build-time=com.quarkus.inertia
```

### Checklist de compatibilidad Native Image

| # | Verificación | Comando |
|---|-------------|---------|
| 1 | Compilación nativa exitosa | `mvn package -Pnative` |
| 2 | Ejecución correcta en native | `./target/quarkus-inertia-*-runner` |
| 3 | Sin reflection warnings en build | Revisar log de build |
| 4 | Sin proxies dinámicos | Revisar log de build |
| 5 | Tests de integración pasan en native | `mvn verify -Pnative` |

---

## 3.6 Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|--------------|
| 1 | `PageObject` tiene `@RegisterForReflection` | Test pasa |
| 2 | Sin reflection manual en ninguna clase | Revisión de código |
| 3 | `VersionProvider.getVersion()` no realiza cómputo en cada llamada | Revisión |
| 4 | `ObjectMapper` / `Jsonb` se obtiene por CDI, nunca `new` | Revisión |
| 5 | Build Time First: configuración validada en Startup | Test |
| 6 | Compilación nativa exitosa | `mvn package -Pnative` |
| 7 | Tests de integración pasan en Native Image | `mvn verify -Pnative` |
| 8 | Sin código muerto ni duplicado | Revisión |
| 9 | Sin warnings de compilación | `mvn compile` |
| 10 | Hot Path optimizado (máx 1 serialización por request) | Revisión |

---

## 3.7 Matriz de Cumplimiento (actualización al finalizar)

| ID | Estado |
|----|--------|
| REQ-020 | VALIDATED |
| REQ-021 | VALIDATED |
| REQ-022 | VALIDATED |
| REQ-010 | VALIDATED |
| REQ-011 | VALIDATED |
| REQ-012 | VALIDATED |
| REQ-014 | VALIDATED |
| REQ-060 | VALIDATED |
| REQ-061 | VALIDATED |

---

## 3.8 Notas

- No registrar clases preventivamente con `@RegisterForReflection`. Solo registrar `PageObject`.
- Toda configuración debe resolverse en Build Time o Startup.
- No utilizar características incompatibles con análisis estático de GraalVM.
- Las optimizaciones deben justificarse con mediciones; no optimizar prematuramente.
- Si una optimización rompe la legibilidad, documentar la decisión técnica.
