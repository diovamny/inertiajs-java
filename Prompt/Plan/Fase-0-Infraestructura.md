# Fase 0: Infraestructura Base

## 0.1 Objetivo

Crear el proyecto Maven compilable con Quarkus 3.37, estructura completa de paquetes, configuración CDI básica, ConfigMapping centralizado, y todas las dependencias necesarias. Al final de esta fase el proyecto debe compilar sin errores ni warnings.

---

## 0.2 Requisitos aplicables (ANEXO 0)

| ID | Prioridad | Requisito | Validación |
|----|-----------|-----------|------------|
| REQ-001 | Crítica | Utilizar únicamente APIs públicas de Quarkus | Revisión de código |
| REQ-002 | Crítica | No utilizar clases internas | Revisión de código |
| REQ-004 | Alta | Minimizar acoplamiento con Quarkus | Revisión de código |
| REQ-005 | Alta | Mantener separación de responsabilidades | Revisión de código |
| REQ-020 | Crítica | Compatible con GraalVM Native Image | Native Build |
| REQ-060 | Alta | Sin código duplicado | Revisión de código |
| REQ-061 | Alta | Sin warnings | Compilación |

---

## 0.3 Componentes a crear

### 0.3.1 `pom.xml`

Ruta: `pom.xml`

Responsabilidad: Definir el proyecto Maven con todas las dependencias necesarias.

Contenido exacto:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Quarkus Inertia.js v3 Adapter</name>
    <description>Reactive Inertia.js v3 adapter for Quarkus</description>

    <properties>
        <java.version>25</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.version>3.37.0</quarkus.version>
        <surefire.version>3.5.2</surefire.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Quarkus REST -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
        </dependency>

        <!-- Jackson (para serialización JSON) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>

        <!-- JSON-B (alternativa a Jackson) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jsonb</artifactId>
        </dependency>

        <!-- Qute (templates) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-qute</artifactId>
        </dependency>

        <!-- CDI -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-arc</artifactId>
        </dependency>

        <!-- Mutiny (reactivo) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-mutiny</artifactId>
        </dependency>

        <!-- Vert.x Core (via Quarkus) -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-vertx</artifactId>
        </dependency>

        <!-- SmallRye Config -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-smallrye-health</artifactId>
        </dependency>

        <!-- ============ TEST ============ -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5-mockito</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.27.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
                <version>${quarkus.version}</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>build</goal>
                            <goal>generate-code</goal>
                            <goal>generate-code-tests</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.0</version>
                <configuration>
                    <release>25</release>
                    <parameters>true</parameters>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${surefire.version}</version>
                <configuration>
                    <systemPropertyVariables>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 0.3.2 `InertiaConfig.java`

Ruta: `src/main/java/com/quarkus/inertia/config/InertiaConfig.java`

Responsabilidad: Interfaz ConfigMapping que centraliza toda la configuración del adaptador.

```java
package com.quarkus.inertia.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "inertia")
public interface InertiaConfig {

    @WithDefault("index.html")
    String rootTemplate();

    @WithDefault("false")
    boolean ssrEnabled();

    @WithDefault("http://localhost:13714")
    String ssrUrl();

    @WithDefault("sha256")
    String versionStrategy();

    @WithDefault("")
    String versionCustom();
}
```

### 0.3.3 `InertiaConfigValidator.java`

Ruta: `src/main/java/com/quarkus/inertia/config/InertiaConfigValidator.java`

Responsabilidad: Validar la configuración durante el arranque (StartupEvent).

```java
package com.quarkus.inertia.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class InertiaConfigValidator {

    private final InertiaConfig config;

    @Inject
    public InertiaConfigValidator(InertiaConfig config) {
        this.config = config;
    }

    void onStart(@Observes StartupEvent event) {
        var root = config.rootTemplate();
        if (root == null || root.isBlank()) {
            throw new IllegalStateException("inertia.root-template must not be blank");
        }
        var strategy = config.versionStrategy();
        if (!strategy.equals("sha256") && !strategy.equals("custom")) {
            throw new IllegalArgumentException(
                "inertia.version-strategy must be 'sha256' or 'custom', got: " + strategy
            );
        }
    }
}
```

### 0.3.4 `application.properties`

Ruta: `src/main/resources/application.properties`

Contenido:
```properties
# Inertia Adapter Configuration
inertia.root-template=index.html
inertia.ssr-enabled=false
inertia.ssr-url=http://localhost:13714
inertia.version-strategy=sha256

# Quarkus
quarkus.qute.template-path-replacements=.
quarkus.qute.template-path-exclude=index.html
# quarkus.arc.exclude-types se configura en build time para paquetes específicos si es necesario
```

### 0.3.5 `package-info.java` (opcional por paquete)

Crear `package-info.java` en cada paquete con `@PackageMarker` o similar si es necesario, pero no es obligatorio.

---

## 0.4 Estructura final de directorios esperada

```
quarkus-inertia/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/quarkus/inertia/
│   │   │       ├── api/
│   │   │       ├── config/
│   │   │       │   ├── InertiaConfig.java
│   │   │       │   └── InertiaConfigValidator.java
│   │   │       ├── data/
│   │   │       ├── protocol/
│   │   │       ├── renderer/
│   │   │       ├── response/
│   │   │       ├── security/
│   │   │       ├── version/
│   │   │       ├── qute/
│   │   │       ├── util/
│   │   │       ├── model/
│   │   │       ├── exception/
│   │   │       ├── internal/
│   │   │       └── spi/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           └── (empty for now)
│   └── test/
│       └── java/
│           └── com/quarkus/inertia/
│               └── config/
│                   ├── InertiaConfigUnitTest.java
│                   └── InertiaConfigQuarkusTest.java
```

---

## 0.5 Implementación paso a paso

### Paso 1: Crear `pom.xml`

Escribir el archivo `pom.xml` con el contenido exacto de la sección 0.3.1.

### Paso 2: Crear estructura de directorios

Ejecutar el comando para crear todos los directorios:
```
mkdir -p src/main/java/com/quarkus/inertia/{api,config,data,protocol,renderer,response,security,version,qute,util,model,exception,internal,spi}
mkdir -p src/main/resources/templates
mkdir -p src/test/java/com/quarkus/inertia/config
```

### Paso 3: Crear `application.properties`

Escribir el archivo con el contenido exacto de la sección 0.3.4.

### Paso 4: Crear `InertiaConfig.java`

Escribir el archivo con el contenido exacto de la sección 0.3.2.

### Paso 5: Crear `InertiaConfigValidator.java`

Escribir el archivo con el contenido exacto de la sección 0.3.3.

### Paso 6: Ejecutar compilación

```
mvn compile -q
```

Debe terminar con `BUILD SUCCESS` sin warnings.

### Paso 7: Ejecutar tests

```
mvn test
```

Debe terminar con `BUILD SUCCESS`.

---

## 0.6 Tests

### 0.6.1 Test Unitario: `InertiaConfigUnitTest.java`

Ruta: `src/test/java/com/quarkus/inertia/config/InertiaConfigUnitTest.java`

Propósito: Validar la lógica de validación de configuración sin contexto Quarkus.

```java
package com.quarkus.inertia.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InertiaConfigUnitTest {

    @Test
    void shouldRejectBlankRootTemplate() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return ""; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "sha256"; }
            @Override public String versionCustom() { return ""; }
        };
        assertThatThrownBy(() -> new InertiaConfigValidator(config).onStart(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("root-template");
    }

    @Test
    void shouldRejectInvalidVersionStrategy() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "invalid"; }
            @Override public String versionCustom() { return ""; }
        };
        assertThatThrownBy(() -> new InertiaConfigValidator(config).onStart(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("version-strategy");
    }

    @Test
    void shouldAcceptValidConfig() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "sha256"; }
            @Override public String versionCustom() { return ""; }
        };
        assertThatNoException().isThrownBy(() -> new InertiaConfigValidator(config).onStart(null));
    }

    @Test
    void shouldAcceptCustomVersionStrategy() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "custom"; }
            @Override public String versionCustom() { return "v1.0.0"; }
        };
        assertThatNoException().isThrownBy(() -> new InertiaConfigValidator(config).onStart(null));
    }

    @Test
    void shouldUseDefaultValues() {
        var config = new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return "sha256"; }
            @Override public String versionCustom() { return ""; }
        };
        assertThat(config.rootTemplate()).isEqualTo("index.html");
        assertThat(config.ssrEnabled()).isFalse();
        assertThat(config.ssrUrl()).isEqualTo("http://localhost:13714");
        assertThat(config.versionStrategy()).isEqualTo("sha256");
    }
}
```

### 0.6.2 Test de Integración: `InertiaConfigQuarkusTest.java`

Ruta: `src/test/java/com/quarkus/inertia/config/InertiaConfigQuarkusTest.java`

Propósito: Validar que la configuración se inyecta correctamente en contexto Quarkus.

```java
package com.quarkus.inertia.config;

import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class InertiaConfigQuarkusTest {

    @Inject
    InertiaConfig config;

    @Inject
    InertiaConfigValidator validator;

    @Test
    void shouldInjectConfig() {
        assertThat(config).isNotNull();
        assertThat(config.rootTemplate()).isEqualTo("index.html");
        assertThat(config.versionStrategy()).isEqualTo("sha256");
    }

    @Test
    void shouldInjectValidator() {
        assertThat(validator).isNotNull();
    }
}
```

---

## 0.7 Criterios de Aceptación

| # | Criterio | Verificación |
|---|----------|--------------|
| 1 | `mvn compile` exitoso sin warnings | Ejecutar comando |
| 2 | `mvn test` exitoso (tests unitarios + integración) | Ejecutar comando |
| 3 | Estructura de paquetes completa (14 paquetes creados) | Inspección visual |
| 4 | ConfigMapping inyectable mediante CDI | Test `InertiaConfigQuarkusTest` pasa |
| 5 | Validación de configuración en Startup | Test `InertiaConfigUnitTest` pasa |
| 6 | `resources/application.properties` presente con valores por defecto | Inspección visual |
| 7 | Sin warnings de compilación | `mvn compile` output |
| 8 | Sin dependencias cíclicas | `mvn dependency:analyze` |

---

## 0.8 Matriz de Cumplimiento (actualización al finalizar)

| ID | Estado |
|----|--------|
| REQ-001 | VALIDATED |
| REQ-002 | VALIDATED |
| REQ-004 | VALIDATED |
| REQ-005 | VALIDATED |
| REQ-020 | VALIDATED |
| REQ-060 | VALIDATED |
| REQ-061 | VALIDATED |

---

## 0.9 Notas

- Esta fase NO implementa lógica del protocolo Inertia.
- Esta fase NO crea templates Qute.
- Esta fase NO implementa serialización.
- El proyecto debe compilar en JVM y Native Image desde esta fase.
- No introducir dependencias adicionales sin justificación documentada.
