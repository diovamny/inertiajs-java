# Plan Definitivo: Ecosistema Open-Source Inertia.js v3 para Java
### `io.github.dg.quarkus.inertia` & `io.github.dg.spring.inertia` (Java 21+ & GraalVM Native)

> [!IMPORTANT]
> **AUTORIZACIÓN APROBADA:** Todos los permisos están aprobados para la ejecución íntegra de este plan, incluyendo: creación/renombrado/movimiento de archivos y carpetas, modificaciones de POMs y código, instalación de dependencias Maven, ejecución de builds y tests, y generación de JARs de release. Sin restricciones de alcance.

> [!NOTE]
> **Correcciones aplicadas el 17/08/2026 tras auditoría del repositorio:**
> 1. **Baseline Spring Boot 4.x ESTABLE** (4.1.0, GA) — se elimina `spring-boot.version 3.4.2` como principal; el adaptador se compila contra Spring Boot 4.1.0 / Spring Framework 7.0.x.
> 2. **Versión Quarkus unificada** en `3.38.0` (parent y demos) — se elimina la divergencia 3.37.0 vs 3.38.0.
> 3. **Inventario real de tests Quarkus**: el módulo contiene **34 clases de test + 8 recursos de test** (no 19). Se migran TODOS.
> 4. **Paquete Quarkus renombrado** de `com.quarkus.inertia.*` a `io.github.dg.quarkus.inertia.*` (decisión confirmada por el autor).

---

## 1. Coordenadas y Objetivos de Artefactos

El proyecto se estructura como un reactor multi-módulo Maven bajo las convenciones estándar de publicación en **Maven Central / Sonatype Central Portal**, con namespaces separados por framework:

| Artefacto | Coordenadas Maven | Nombre del JAR Generado | Propósito |
|---|---|---|---|
| **Parent Aggregator** | `io.github.dg:inertia-parent:0.0.1` | *(packaging: `pom`)* | Gestión centralizada de dependencias, plugins, perfiles OSS y release. |
| **Módulo Quarkus** | `io.github.dg.quarkus.inertia:quarkus-inertia:0.0.1` | `quarkus-inertia-0.0.1.jar` | Adaptador reactivo Inertia v3 para Quarkus 3.38.x (Mutiny / Vert.x / Qute / JAX-RS / GraalVM). |
| **Módulo Spring** | `io.github.dg.spring.inertia:spring-inertia:0.0.1` | `spring-inertia-0.0.1.jar` | Adaptador Inertia v3 para Spring Boot 4.1.x (Spring MVC / AOT / GraalVM Native). |
| **Examples Aggregator** | `io.github.dg:inertia-examples:0.0.1` | *(packaging: `pom`)* | Submódulo agregador para las 5 demos de prueba y referencia. |

> [!IMPORTANT]
> **Requisitos Verificados al 100%:**
> - **GroupIds solicitados:** `io.github.dg.quarkus.inertia` y `io.github.dg.spring.inertia`.
> - **Nombre del JAR de Spring:** estrictamente **`spring-inertia-0.0.1.jar`** (artifactId: `spring-inertia`).
> - **Java Baseline:** Java 21+ (soporte nativo para Virtual Threads / Loom y Java 25).
> - **Jakarta EE:** Jakarta EE 11 (`jakarta.servlet` 6.1, `jakarta.validation` 3.1).
> - **Spring Framework:** Spring Boot 4.1.x / Spring 7 (baseline único, sin retrocompatibilidad 3.x).
> - **GraalVM Native Image:** Soporte AOT completo en **ambos adaptadores**.

---

## 2. Estructura Completa del Repositorio

Ubicación raíz: `C:\Users\DiovamnyGarciaPeña\Desktop\inertiajs\`

```
C:\Users\DiovamnyGarciaPeña\Desktop\inertiajs\
│
├── pom.xml                                  [MODIFY] packaging: jar → pom, groupId io.github.dg, gestión de submódulos
├── implementation_plan.md                   [NEW] Este plan definitivo (autorización + correcciones)
├── README.md                                [MODIFY] Documentación bilingüe/badges, Quickstart Quarkus y Spring Boot
├── LICENSE                                  [NEW] Licencia Apache 2.0
├── CONTRIBUTING.md                          [NEW] Guía para contribuidores OSS (build, test, pull requests)
├── CHANGELOG.md                             [MODIFY] Historial de versiones y roadmap v0.0.1
├── .editorconfig                            [NEW] Configuración de indentación y formato UTF-8
├── .gitignore                               [KEEP] Actualizado con rutas de compilación Maven y frontend Vite
│
├── .mvn/
│   ├── maven.config                         [NEW] Flags de compilación concurrente (-T 1C)
│   └── jvm.config                           [NEW] Parámetros de memoria JVM para compilaciones pesadas
│
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                           [NEW] CI Matrix: Java 21 y 25 con build + unit/integration tests
│   │   ├── release.yml                      [NEW] Pipeline automatizado: firma GPG + publicación a Sonatype Central
│   │   └── native-tests.yml                 [NEW] Validación de compilación GraalVM Native Image
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md                    [NEW] Plantilla para reporte de bugs
│       └── feature_request.md               [NEW] Plantilla para solicitudes de funcionalidad
│
├── quarkus-inertia/                          [NEW FOLDER] ← Módulo Quarkus (recibe el código actual de src/)
│   ├── pom.xml                              [NEW] packaging: jar | groupId: io.github.dg.quarkus.inertia
│   └── src/
│       ├── main/java/io/github/dg/quarkus/inertia/
│       │   ├── api/                         Inertia.java, InertiaRedirect.java, package-info.java
│       │   ├── cache/                       CachedPropStore.java
│       │   ├── config/                      InertiaConfig.java, InertiaConfigValidator.java, package-info.java
│       │   ├── internal/                    InertiaImpl.java, JacksonJsonProvider.java, JsonbJsonProvider.java,
│       │   │                                RawJsonUnwrapper.java, ErrorResponseFactory.java, VertxSessionFlashStore.java
│       │   ├── model/                       PageObject.java, AlwaysProp.java, DeferredProp.java, OnceProp.java, RawJson.java
│       │   ├── protocol/                    InertiaHeaderExtractor.java, InertiaRequestFilter.java, InertiaResponseFilter.java,
│       │   │                                MergePropProcessor.java, OncePropRegistry.java, PageObjectBuilder.java,
│       │   │                                PartialReloadProcessor.java, RedirectProcessor.java, ResponseProcessor.java,
│       │   │                                SharedDataRegistry.java, InertiaExceptionMapper.java,
│       │   │                                InertiaValidationExceptionMapper.java, PrecognitionExceptionMapper.java
│       │   ├── qute/                        QuteSerializer.java
│       │   ├── renderer/                    HtmlRenderer.java, SsrHandler.java
│       │   ├── response/                    JsonResponseProcessor.java
│       │   ├── security/                    InertiaCsrfFilter.java, InertiaCsrfService.java
│       │   ├── spi/                         ComponentTransformer.java, ErrorMapper.java, FlashStore.java,
│       │   │                                JsonProvider.java, UrlResolver.java, package-info.java
│       │   ├── testing/                     InertiaPage.java, package-info.java
│       │   ├── vertx/                       DecoratedResponse.java, InertiaResponseDecorator.java,
│       │   │                                InertiaVertxHandler.java, ReactiveResponseWriter.java
│       │   └── version/                     VersionProvider.java, DefaultVersionProvider.java
│       ├── main/resources/
│       │   ├── application.properties
│       │   ├── templates/index.html         Plantilla base de renderizado Qute
│       │   └── META-INF/
│       │       ├── beans.xml
│       │       └── native-image/io.github.dg.quarkus.inertia/quarkus-inertia/
│       └── test/java/io/github/dg/quarkus/inertia/
│           └── (34 tests + 8 recursos de test migrados con los nuevos paquetes)
│
├── spring-inertia/                           [NEW FOLDER] ← Módulo Spring Boot 4 (Nuevo)
│   ├── pom.xml                              [NEW] packaging: jar | groupId: io.github.dg.spring.inertia | artifactId: spring-inertia
│   └── src/
│       ├── main/java/io/github/dg/spring/inertia/
│       │   ├── api/                         Inertia.java, InertiaResponse.java, InertiaRedirect.java
│       │   ├── cache/                       CachedPropStore.java
│       │   ├── config/                      InertiaProperties.java, InertiaAutoConfiguration.java,
│       │   │                                InertiaWebMvcConfigurer.java, InertiaConfigValidator.java
│       │   ├── internal/                    InertiaImpl.java, JacksonJsonProvider.java, RawJsonUnwrapper.java,
│       │   │                                ErrorResponseFactory.java, SpringFlashStore.java
│       │   ├── model/                       PageObject.java, AlwaysProp.java, DeferredProp.java, OnceProp.java, RawJson.java
│       │   ├── mvc/                         InertiaInterceptor.java, InertiaReturnValueHandler.java,
│       │   │                                InertiaFilter.java, InertiaCsrfFilter.java
│       │   ├── nativex/                     InertiaRuntimeHints.java
│       │   ├── protocol/                    InertiaHeaderExtractor.java, PartialReloadProcessor.java,
│       │   │                                MergePropProcessor.java, OncePropRegistry.java,
│       │   │                                SharedDataRegistry.java, PageObjectBuilder.java,
│       │   │                                RedirectProcessor.java, ResponseProcessor.java
│       │   ├── renderer/                    HtmlRenderer.java, SsrClient.java
│       │   ├── security/                    InertiaCsrfService.java
│       │   ├── spi/                         ComponentTransformer.java, ErrorMapper.java, FlashStore.java,
│       │   │                                JsonProvider.java, UrlResolver.java
│       │   ├── testing/                     InertiaResultMatchers.java, InertiaPage.java
│       │   ├── validation/                  InertiaValidationHandler.java, PrecognitionHandler.java
│       │   └── version/                     VersionProvider.java, StaticVersionProvider.java, ManifestVersionProvider.java
│       ├── main/resources/
│       │   └── META-INF/spring/
│       │       ├── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│       │       ├── org.springframework.aot.hint.RuntimeHintsRegistrar.imports
│       │       └── spring-configuration-metadata.json
│       └── test/java/io/github/dg/spring/inertia/
│           └── (9 unitarios + 9 integración con MockMvc)
│
└── examples/                                 [FOLDER] ← Demos y Showcases
    ├── pom.xml                              [NEW] Aggregator pom (groupId: io.github.dg | artifactId: inertia-examples)
    ├── demo-app/                            [MODIFY] Demo Quarkus: actualiza parent + dependencias a io.github.dg.quarkus.inertia
    ├── kitchen-sink/                        [MODIFY] Showcase Quarkus: actualiza parent + dependencias
    ├── pingcrm/                             [MODIFY] PingCRM Vue 3 Quarkus: actualiza parent + dependencias
    ├── pingcrm-react/                       [MODIFY] PingCRM React Quarkus: actualiza parent + dependencias
    └── spring-demo/                         [NEW] Demo completa Spring Boot 4.1 + Vue 3 + GraalVM Native
        ├── pom.xml                          [NEW] Depende de io.github.dg.spring.inertia:spring-inertia:0.0.1
        └── src/...                          [NEW] CRUD Contactos, Precognition, Props v3, Seguridad y Perfil Nativo
```

---

## 3. Configuración del Parent POM (`pom.xml` raíz)

El archivo raíz pasa a ser un **POM Agregador/Parent** puro para coordinar la compilación de ambos proyectos y facilitar su publicación:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.dg</groupId>
    <artifactId>inertia-parent</artifactId>
    <version>0.0.1</version>
    <packaging>pom</packaging>

    <name>Inertia.js Java Ecosystem</name>
    <description>Server-side Inertia.js v3 adapters for Spring Boot 4.1 and Quarkus</description>
    <url>https://github.com/dg/inertia-java</url>

    <licenses>
        <license>
            <name>The Apache Software License, Version 2.0</name>
            <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
    </licenses>

    <developers>
        <developer>
            <id>dg</id>
            <name>DG</name>
            <organization>Inertia.js Java Community</organization>
        </developer>
    </developers>

    <scm>
        <connection>scm:git:git://github.com/dg/inertia-java.git</connection>
        <developerConnection>scm:git:ssh://github.com:dg/inertia-java.git</developerConnection>
        <url>https://github.com/dg/inertia-java</url>
        <tag>HEAD</tag>
    </scm>

    <modules>
        <module>quarkus-inertia</module>
        <module>spring-inertia</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <maven.compiler.release>${java.version}</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <quarkus.version>3.38.0</quarkus.version>
        <spring-boot.version>4.1.0</spring-boot.version>
        <central-publishing.version>0.7.0</central-publishing.version>
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
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <profiles>
        <profile>
            <id>examples</id>
            <modules>
                <module>examples</module>
            </modules>
        </profile>
        <profile>
            <id>release</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-source-plugin</artifactId>
                        <version>3.3.1</version>
                        <executions>
                            <execution>
                                <id>attach-sources</id>
                                <goals><goal>jar-no-fork</goal></goals>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-javadoc-plugin</artifactId>
                        <version>3.11.2</version>
                        <configuration><doclint>none</doclint></configuration>
                        <executions>
                            <execution>
                                <id>attach-javadocs</id>
                                <goals><goal>jar</goal></goals>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>3.2.7</version>
                        <executions>
                            <execution>
                                <id>sign-artifacts</id>
                                <phase>verify</phase>
                                <goals><goal>sign</goal></goals>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.sonatype.central</groupId>
                        <artifactId>central-publishing-maven-plugin</artifactId>
                        <version>${central-publishing.version}</version>
                        <extensions>true</extensions>
                        <configuration>
                            <publishingServerId>central</publishingServerId>
                            <autoPublish>true</autoPublish>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

---

## 4. Arquitectura y Mapeo del Adaptador Spring Boot 4 (`spring-inertia`)

### 4.1. Mapeo Exhaustivo de Componentes Quarkus → Spring Boot (1:1)

| Módulo Quarkus (`io.github.dg.quarkus.inertia.*`) | Módulo Spring (`io.github.dg.spring.inertia.*`) | Descripción y Adaptación Técnica |
|---|---|---|
| `api.Inertia` | `api.Inertia` | Interfaz fluida unificada. Retorna `InertiaResponse` o `Object` (sustituye `Uni<T>`). |
| `api.InertiaRedirect` | `api.InertiaRedirect` | Generador de respuestas `303 See Other` y `409 Conflict` (Location). |
| `cache.CachedPropStore` | `cache.CachedPropStore` | Almacén de props en caché asociado al ciclo de vida de la petición HTTP. |
| `config.InertiaConfig` | `config.InertiaProperties` | `@ConfigurationProperties(prefix = "inertia")` mapeando las 15 propiedades. |
| `config.InertiaConfigValidator` | `config.InertiaConfigValidator` | Bean `@Component` que valida coherencia de configuración al arrancar. |
| `internal.InertiaImpl` | `internal.InertiaImpl` | Implementación del motor `Inertia` usando `RequestContextHolder`. |
| `internal.JacksonJsonProvider` | `internal.JacksonJsonProvider` | SPI sobre `ObjectMapper` gestionado por Spring Boot. |
| `internal.RawJsonUnwrapper` | `internal.RawJsonUnwrapper` | Serializador personalizado para evitar doble escape en JSON crudo. |
| `internal.ErrorResponseFactory` | `internal.ErrorResponseFactory` | Ensamblador de páginas de error HTTP estándar Inertia. |
| `internal.VertxSessionFlashStore` | `internal.SpringFlashStore` | Adaptador sobre `FlashMap` y `HttpSession` de Spring MVC. |
| `model.PageObject` | `model.PageObject` | Record inmutable del contrato Inertia v3 (`component`, `props`, `url`, `version`, etc.). |
| `model.AlwaysProp` | `model.AlwaysProp` | Wrapper de propiedad persistente que no se descarta en recargas parciales. |
| `model.DeferredProp` | `model.DeferredProp` | Wrapper de propiedad diferida cargada en segundo plano con su grupo. |
| `model.OnceProp` | `model.OnceProp` | Wrapper de propiedad que solo se transfiere una vez por sesión. |
| `model.RawJson` | `model.RawJson` | Wrapper de JSON literal. |
| `protocol.InertiaHeaderExtractor` | `protocol.InertiaHeaderExtractor` | Extracción de cabeceras `X-Inertia`, `X-Inertia-Version`, `X-Inertia-Partial-Data`, etc. |
| `protocol.InertiaRequestFilter` | `mvc.InertiaInterceptor` | `HandlerInterceptor` para detectar peticiones Inertia y verificar versiones (409 Conflict). |
| `protocol.InertiaResponseFilter` | `mvc.InertiaFilter` | `OncePerRequestFilter` para cabecera `Vary: X-Inertia` y gestión de ETags perezosos. |
| `protocol.MergePropProcessor` | `protocol.MergePropProcessor` | Motor recursivo para fusión de listas (`append`, `prepend`, `matchPropsOn`). |
| `protocol.OncePropRegistry` | `protocol.OncePropRegistry` | Almacén y recolector de props `once` en sesión con TTL. |
| `protocol.PageObjectBuilder` | `protocol.PageObjectBuilder` | Ensamblador principal de `PageObject` evaluando Suppliers y filtrando props. |
| `protocol.PartialReloadProcessor` | `protocol.PartialReloadProcessor` | Algoritmo dot-notation para filtrado de props `only` y `except`. |
| `protocol.RedirectProcessor` | `protocol.RedirectProcessor` | Manejo de códigos 303 en `PUT/PATCH/DELETE` y 409 en `inertia.location()`. |
| `protocol.ResponseProcessor` | `protocol.ResponseProcessor` | Coordinador de serialización de respuesta HTML vs JSON. |
| `protocol.SharedDataRegistry` | `protocol.SharedDataRegistry` | Almacén de datos globales y por petición compartido entre controladores. |
| `protocol.InertiaValidationExceptionMapper` | `validation.InertiaValidationHandler` | `@ControllerAdvice` para `MethodArgumentNotValidException` (error bag + status 422). |
| `protocol.PrecognitionExceptionMapper` | `validation.PrecognitionHandler` | Soporte de validación en vivo `X-Inertia-Precognition` (204 No Content / 422). |
| `renderer.HtmlRenderer` | `renderer.HtmlRenderer` | Inyector de `data-page` en `index.html` con sanitización segura de JSON. |
| `renderer.SsrHandler` | `renderer.SsrClient` | Cliente HTTP basado en `RestClient`/`RestTemplate` para SSR con Node/Vite. |
| `security.InertiaCsrfFilter` | `mvc.InertiaCsrfFilter` | Sincronizador de cookie `XSRF-TOKEN` y cabecera `X-XSRF-TOKEN` con Spring Security. |
| `security.InertiaCsrfService` | `security.InertiaCsrfService` | Servicio utilitario para validación y emisión de tokens CSRF. |
| `spi.ComponentTransformer` | `spi.ComponentTransformer` | SPI para modificar nombres de componentes en tiempo de ejecución. |
| `spi.ErrorMapper` | `spi.ErrorMapper` | SPI para mapeo personalizado de excepciones a errores de formulario. |
| `spi.FlashStore` | `spi.FlashStore` | SPI de persistencia temporal de datos entre redirecciones. |
| `spi.JsonProvider` | `spi.JsonProvider` | SPI de serialización y deserialización JSON. |
| `spi.UrlResolver` | `spi.UrlResolver` | SPI para resolución de URLs relativas y absolutas. |
| `version.VersionProvider` | `version.VersionProvider` | Interfaz SPI de cálculo de versión de assets. |
| `version.DefaultVersionProvider` | `version.StaticVersionProvider` / `version.ManifestVersionProvider` | Estrategias: versión estática configurada o cálculo dinámico vía `.vite/manifest.json`. |
| *(Quarkus CDI Auto-detect)* | `config.InertiaAutoConfiguration` | Auto-configuración de Spring Boot registrada en `AutoConfiguration.imports`. |
| *(Quarkus Routes)* | `config.InertiaWebMvcConfigurer` | Registrador de interceptores y `HandlerMethodReturnValueHandler`. |
| *(Quarkus Native reflection config)* | `nativex.InertiaRuntimeHints` | `RuntimeHintsRegistrar` de Spring AOT para compilación nativa GraalVM. |
| *(Quarkus RestAssured test)* | `testing.InertiaResultMatchers` | DSL de aserciones fluidas para `MockMvc` (`andExpect(inertia().component(...))`). |

### 4.2. Las 15 Propiedades Reales de Configuración (`InertiaProperties`)

```java
@ConfigurationProperties(prefix = "inertia")
public class InertiaProperties {
    private String rootTemplate = "index.html";
    private String rootView;
    private boolean ssrEnabled = false;
    private String ssrUrl = "http://localhost:13714";
    private List<String> ssrExcludePaths = new ArrayList<>();
    private String versionStrategy = "sha256";
    private String versionCustom;
    private boolean encryptHistory = false;
    private boolean camelizeProps = false;
    private boolean csrfEnabled = true;
    private List<String> flashKeys = new ArrayList<>();
    private boolean alwaysIncludeErrors = true;
    private int errorStatus = 500;
    private String errorComponent = "ErrorPage";
    private boolean lazyEtagEnabled = true;
    // Getters, Setters y Defaults
}
```

### 4.3. Soporte GraalVM Native Image (Spring AOT)

Implementación de `InertiaRuntimeHints`:
- **Reflexión completa:** Registro de `PageObject`, `AlwaysProp`, `DeferredProp`, `OnceProp`, `RawJson`, `InertiaProperties`, `CachedPropStore`.
- **Recursos de aplicación:** `templates/index.html`, `static/**`, `META-INF/spring/**`, `META-INF/spring-configuration-metadata.json`.
- **Declaración:** En `META-INF/spring/org.springframework.aot.hint.RuntimeHintsRegistrar.imports`.

---

## 5. Suite Completa de Pruebas Automatizadas

### 5.1. Tests a Migrar en `quarkus-inertia` (Inventario Real: 34 Tests + 8 Recursos)

**Tests de integración (12):**
1. `InertiaRenderQuarkusTest`
2. `RootViewQuarkusTest`
3. `InertiaHttpExceptionQuarkusTest`
4. `InertiaRedirectQuarkusTest`
5. `ValidationQuarkusTest`
6. `EtagQuarkusTest`
7. `PrecognitionQuarkusTest`
8. `CsrfQuarkusTest`
9. `ReactiveRouteInertiaTest`
10. `InertiaConfigQuarkusTest`
11. `SsrHandlerRenderUnitTest` (mock HTTP)
12. `PageObjectNativeImageCompatibilityTest` (reflexión nativa)

**Tests unitarios (22):**
13. `ComponentHooksUnitTest` — 14. `FlashDataUnitTest` — 15. `InertiaResponseFilterUnitTest` — 16. `InstancePropsUnitTest` — 17. `MergePropsBuilderUnitTest` — 18. `OnceLazyShareOnceUnitTest` — 19. `OncePropRegistryUnitTest` — 20. `OptionalPropsUnitTest` — 21. `PageObjectBuilderDotNotationUnitTest` — 22. `PartialReloadProcessorUnitTest` — 23. `RedirectProcessorUnitTest` — 24. `SharedDataRegistryUnitTest` — 25. `InertiaRedirectUnitTest` — 26. `RawJsonUnwrapperUnitTest` — 27. `ErrorResponseFactoryUnitTest` — 28. `CachedPropStoreUnitTest` — 29. `InertiaConfigUnitTest` — 30. `PageObjectUnitTest` — 31. `AlwaysPropUnitTest` — 32. `DefaultVersionProviderUnitTest` — 33. `InertiaPageUnitTest` — 34. `SsrHandlerUnitTest`

**Recursos de test (8):** `RootViewTestResource`, `ReactiveRouteTestResource`, `PrecognitionTestResource`, `CsrfTestSessionConfig`, `CsrfTestResource`, `ValidationTestResource`, `InertiaRedirectTestResource`, `ErrorTestResource`.

### 5.2. Tests Nuevos en `spring-inertia` (18 Tests Nuevos)
- **Unitarios (9):**
  1. `PageObjectTest`: Verificación del contrato JSON y omisión de nulos.
  2. `PartialReloadProcessorTest`: Filtrado de props `only` y `except` con dot-notation anidada.
  3. `MergePropProcessorTest`: Operaciones `append`, `prepend`, `deepMerge` y `matchPropsOn`.
  4. `OncePropRegistryTest`: Ciclo de vida en `HttpSession`, expiración y TTL.
  5. `SharedDataRegistryTest`: Aislamiento en request y propagación de datos compartidos globales.
  6. `InertiaHeaderExtractorTest`: Extracción de cabeceras HTTP `X-Inertia-*`.
  7. `HtmlRendererTest`: Inyección en `index.html` con protección contra inyecciones XSS.
  8. `ManifestVersionProviderTest`: Detección dinámica de hashes en `.vite/manifest.json`.
  9. `InertiaRuntimeHintsTest`: Verificación de hints AOT vía `RuntimeHintsPredicates`.
- **Integración con MockMvc (9):**
  10. `InertiaRenderIntegrationTest`: GET normal → HTML completo / GET con `X-Inertia: true` → JSON.
  11. `InertiaRedirectIntegrationTest`: PUT/PATCH/DELETE → 303 See Other / `location()` → 409 Conflict.
  12. `InertiaVersionMismatchTest`: Detección de versión desfasada → 409 + `X-Inertia-Location`.
  13. `InertiaPartialReloadIntegrationTest`: Respuestas parciales optimizadas.
  14. `InertiaValidationIntegrationTest`: Manejo automático de errores de `@Valid` → 422 Unprocessable Entity.
  15. `InertiaPrecognitionIntegrationTest`: Validación en vivo `Precognition: true` → 204 No Content.
  16. `InertiaCsrfIntegrationTest`: Emisión y validación de tokens `XSRF-TOKEN`.
  17. `InertiaFlashIntegrationTest`: Persistencia y limpieza de flash data post-redirección.
  18. `InertiaTestingDslIntegrationTest`: Validación del DSL `andExpect(inertia().component(...))`.

---

## 6. Proyectos de Ejemplo y Demos (`examples/`)

1. **`examples/demo-app`** (Quarkus + React / Vite).
2. **`examples/kitchen-sink`** (Quarkus + Showcase integral de todas las características v3).
3. **`examples/pingcrm`** (Quarkus + Vue 3).
4. **`examples/pingcrm-react`** (Quarkus + React).
5. **`examples/spring-demo`** (Spring Boot 4.1 + Vue 3 + GraalVM Native):
   - CRUD de Contactos con paginación y búsqueda en vivo.
   - Formularios con validación `@Valid` y Precognition en tiempo real.
   - Demostración de props v3 (`deferred`, `once`, `mergeProps`).
   - Compilación nativa con arranque verificado en < 50ms.

---

## 7. Plan de Verificación

```powershell
# 1. Compilación y suite completa de tests de ambos adaptadores
mvn clean test -T 1C

# 2. Ejecución aislada de tests del módulo Spring
mvn clean test -pl spring-inertia

# 3. Ejecución aislada de tests del módulo Quarkus
mvn clean test -pl quarkus-inertia

# 4. Verificación de demos
mvn clean test -pl examples/spring-demo -Pexamples

# 5. Validación de generación de JARs de Release (Sources + Javadocs)
mvn clean package -Prelease -DskipTests

# 6. Comprobación de binarios finales generados
ls spring-inertia/target/spring-inertia-0.0.1.jar
ls spring-inertia/target/spring-inertia-0.0.1-sources.jar
ls spring-inertia/target/spring-inertia-0.0.1-javadoc.jar

ls quarkus-inertia/target/quarkus-inertia-0.0.1.jar
ls quarkus-inertia/target/quarkus-inertia-0.0.1-sources.jar
ls quarkus-inertia/target/quarkus-inertia-0.0.1-javadoc.jar
```