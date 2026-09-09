# Plan para arquetipos Maven de Inertia Java

**Estado:** autorizado para ejecución total — permiso permanente del solicitante para crear
arquetipos, archivos, SVG locales originales y modificar POMs sin volver a preguntar.
**Autorización:** el solicitante otorga permiso autorizado para todo el plan (fases 0–7),
incluida la creación de los cuatro módulos `maven-archetype`, fixtures, logos SVG locales
de diseño propio, README, CI y publicación candidata. No pedir confirmación en cada paso;
proceder de forma autónoma y solo informar al final.
**Autorización de implementación (nativo/Docker):** el solicitante autoriza expresamente
ejecutar la implementación completa del plan de compilación nativa y Dockerfiles en los
cuatro arquetipos —perfiles `native` en los POM generados, `Dockerfile`+`.dockerignore`,
Maven Wrapper, secciones de README, descriptores y reinstalación/verificación— sin
volver a preguntar; solo informar al final.
**Objetivo:** publicar cuatro arquetipos Maven que generen una aplicación Inertia v3 mínima y ejecutable:

- `inertia-spring-vue-archetype` para Spring Boot / Spring MVC con Vue 3.
- `inertia-spring-react-archetype` para Spring Boot / Spring MVC con React.
- `inertia-quarkus-vue-archetype` para Quarkus REST reactivo con Vue 3.
- `inertia-quarkus-react-archetype` para Quarkus REST reactivo con React.

Los cuatro proyectos generados mostrarán una página de bienvenida con “I ♥ Inertia.js”, un enlace accesible a `https://inertiajs.com/`, el identificador visual de Inertia y el logo del framework elegido.

## 0. Autorización permanente y librerías actualizadas (2026)

> Permiso autorizado para todo: no preguntar. El ejecutor queda facultado para crear
> directorios, POMs, descriptores `archetype-metadata.xml`, plantillas Velocity,
> componentes Vue/React, `vite.config.ts`, `package.json`, plantillas HTML, SVGs locales
> originales, tests, workflows y para registrar los módulos en el POM raíz. Si una marca
> no autoriza su logo oficial, usar SVG propio/texto enlazado + `THIRD_PARTY_NOTICES.md`
> (sin copiar arte de terceros).

| Librería | Versión fijada en arquetipos | Origen |
|---|---|---|
| Java | 21 (`maven.compiler.release`) | POM padre del repo |
| Spring Boot | 4.1.0 | `spring-boot.version` del repo |
| Quarkus | 3.38.0 | `quarkus.version` del repo |
| Adaptador Inertia | `${inertiaAdapterVersion}`, por defecto `0.0.1` (release publicada, nunca `SNAPSHOT` local) | propiedad del arquetipo |
| React / React DOM | `^19.2.0` | `examples/.../pingcrm-react` |
| `@inertiajs/react` | `^3.7.0` | ejemplos React |
| `@inertiajs/vue3` / `@inertiajs/vite` | `^3.7.0` | ejemplos Vue/React |
| Vue | `^3.5.39` | `spring-kitchen-sink` |
| Vite | `^8.2.2` | ejemplos recientes |
| `@vitejs/plugin-react` | `^6.1.1` | ejemplos React |
| `@vitejs/plugin-vue` / `vue-tsc` | `^6.0.7` / `^2.2.12` | ejemplos Vue |
| TypeScript | `^5.9.3` | ejemplos Vue |
| `@types/node` | `^22.20.0` | ejemplos |
| Node / npm (frontend-maven-plugin `1.15.1`) | `v22.14.0` / `10.9.2` | `examples/quarkus/pingcrm-react` |
| JUnit / RestAssured / MockMvc | gestionados por BOM Spring Boot / Quarkus | sin versión fija |
| Nativo/Docker | builder `quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-25`, runtime `ubi9/ubi-micro`, Maven Wrapper 3.9.x | decisión expresa del solicitante: mantener `jdk-25` (fallback documentado: `jdk-21`) |

## 1. Decisiones de producto

| Decisión | Elección para la primera versión | Motivo |
|---|---|---|
| Número de arquetipos | Cuatro: Spring+Vue, Spring+React, Quarkus+Vue y Quarkus+React | Cada combinación queda explícita, pequeña y sin POM ni código condicional. |
| Cliente inicial | Vue 3 o React, ambos con TypeScript y Vite | Son los dos clientes solicitados; cada arquetipo fija uno para conservar una generación reproducible. |
| Página de inicio | Una página Inertia `Welcome` | Demuestra el flujo real del adaptador y no un HTML estático ajeno a Inertia. |
| Ruta inicial | `GET /` | Es predecible para el usuario y para pruebas de humo. |
| Versión del adaptador | Propiedad `inertiaAdapterVersion`, fijada a una release publicada | Nunca generar un consumidor contra `SNAPSHOT` ni contra el POM padre del repositorio. |
| Java | 21 por defecto | Es la versión mínima utilizada actualmente por los adaptadores. |
| Logos | SVG locales oficiales de Java, Inertia, Spring/Quarkus y Vue/React (Devicon + Simple Icons) con atribución en `THIRD_PARTY_NOTICES.md` | Evita hotlinking, cambios remotos y dependencias externas en el arranque. |

Inertia necesita que la primera visita devuelva un documento HTML con el punto de montaje y el page object; las visitas posteriores se resuelven mediante respuestas Inertia. Por eso el controller debe invocar el adaptador y la interfaz se debe materializar como una página del cliente, no devolviendo un `String` HTML desde Java. [Protocolo de Inertia](https://inertiajs.com/docs/v3/core-concepts/the-protocol)

## 2. Resultado exacto de cada arquetipo

Al ejecutar el arquetipo se generará un repositorio independiente con:

```text
<artifactId>/
├── pom.xml                           # incluye perfil `native`
├── Dockerfile                        # nativo multi-stage (builder jdk-25, UBI Micro, puerto 8080)
├── .dockerignore
├── mvnw / mvnw.cmd / .mvn/wrapper/   # Maven Wrapper 3.9.x
├── README.md
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/<packageInPathFormat>/
│   │   │   ├── Application.java
│   │   │   └── WelcomeController.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── templates/index.html
│   │   │   └── static/brand/               # Spring: inertia, java, spring, vue/react
│   │   │   └── META-INF/resources/brand/   # Quarkus (sirve desde META-INF): inertia, java, quarkus, vue/react
│   │   │       ├── inertia.svg           # Simple Icons, los 4 arquetipos
│   │   │       ├── java.svg              # Devicon, los 4 arquetipos
│   │   │       ├── spring.svg            # Devicon, solo arquetipos Spring
│   │   │       ├── quarkus.svg           # Devicon, solo arquetipos Quarkus
│   │   │       ├── vue.svg               # Devicon, solo arquetipos Vue
│   │   │       └── react.svg             # Devicon, solo arquetipos React
│   │   └── webui/                        # como en examples/*/src/main/webui
│   │       ├── package.json
│   │       ├── vite.config.ts
│   │       ├── tsconfig.json
│   │       ├── src/app.tsx                # React (entry Vite → assets/app.js)
│   │       ├── src/app.ts                 # Vue (entry Vite → assets/app.js)
│   │       ├── src/pages/Welcome.tsx     # React
│   │       ├── src/pages/Welcome.vue     # Vue
│   │       └── src/styles/app.css
│   └── test/
│       └── java/<packageInPathFormat>/WelcomeControllerTest.java
└── .github/workflows/ci.yml              # opcional, activable por propiedad
```

Cada arquetipo incluye solo los ficheros del cliente que corresponde a su combinación: no se generarán simultáneamente `.tsx` y `.vue`. El nombre real de las rutas de frontend se decidirá al extraer el fixture del demo existente; debe ser idéntico en Vite, la plantilla raíz y la configuración Java. No se incluirán bases de datos, autenticación, SSR, Docker, Tailwind ni dependencias de ejemplo que no sean necesarias para una página inicial.

## 3. Comportamiento de la página de bienvenida

### 3.1 Contrato del controller

Cada arquetipo generará un controller con una sola responsabilidad: responder la ruta `/` a través de la API pública de su adaptador.

| Framework | Tipo de controller | Respuesta esperada |
|---|---|---|
| Spring Boot | `@RestController`, `@GetMapping("/")` | `inertia.render("Welcome", props)` como `Object`, según la API actual de Spring. |
| Quarkus | recurso CDI/JAX-RS `@Path("/")`, `@GET` | `inertia.render("Welcome", props)` como `Uni<Object>`, según la API reactiva actual. |

Las props deben ser pocas, inmutables y sin datos sensibles:

```text
appName          = valor de la propiedad `appName`
framework        = "Spring Boot" o "Quarkus"
frameworkVersion = versión declarada por el arquetipo
inertiaUrl       = "https://inertiajs.com/"
```

No se debe declarar un `Content-Type` fijo que impida al adaptador devolver JSON cuando la petición incluya `X-Inertia`. Las configuraciones de filtros, serialización y plantilla seguirán las usadas por los ejemplos que ya pasan en este repositorio.

### 3.2 Documento HTML raíz administrado por Java

Cada salida contiene un `templates/index.html` para el framework correspondiente:

- **Spring:** plantilla con los marcadores ya reconocidos por `spring-inertia`, incluido el contenedor `#app`, page JSON seguro, CSS y módulo Vite.
- **Quarkus:** plantilla Qute equivalente, con los marcadores que procesa `quarkus-inertia`, incluido el contenedor, page JSON, CSS y módulo Vite.

La plantilla debe contener título, `meta viewport`, favicon y un bloque `noscript` que explique que la aplicación requiere JavaScript. No debe duplicar el contenido de `Welcome`: sin SSR, Inertia entrega un shell HTML y el cliente monta la página después. Esto mantiene el starter conforme al ciclo de vida de Inertia. [Configuración de servidor](https://inertiajs.com/docs/v3/installation/server-side-setup)

### 3.3 Componente `Welcome`

`Welcome.tsx` (React) o `Welcome.vue` (Vue) será el único componente inicial y debe mostrar la misma interfaz y las mismas props:

1. Logo de Inertia con `alt="Inertia.js"`.
2. Encabezado visible: `I ♥ Inertia.js`.
3. Un enlace externo a `https://inertiajs.com/`, con texto `Visit Inertia.js`, `target="_blank"` y `rel="noreferrer"`.
4. Tres bloques con logo y texto: “Powered by Spring Boot/Quarkus” (según starter), “Built with Vue.js/React” (según cliente) y “Running on Java”.
5. El nombre de la aplicación y la versión del framework recibidos desde el controller.
6. Diseño responsive, contraste AA, foco visible y texto alternativo para todos los recursos no decorativos.

El corazón puede ser texto Unicode (`♥`) con una etiqueta accesible; no requiere un recurso de marca. El título, el contenido y los `alt` se escribirán en inglés para que los proyectos generados sean consistentes con el ecosistema Java.

## 4. Logos, marcas y atribuciones

Los logos de Java, Inertia, Spring, Quarkus, Vue y React son activos de marca.
Por autorización expresa del solicitante se incluyen como SVG locales obtenidos de
los proyectos comunitarios Devicon y Simple Icons (sin URL remotas en HTML/CSS),
con atribución de titulares en `THIRD_PARTY_NOTICES.md` y política de retirada o
sustitución por texto enlazado a petición del titular. No se presentan como
propiedad del proyecto ni como patrocinio.

Si alguna marca no permite la redistribución, el arquetipo debe usar texto plano enlazado a su sitio oficial y dejar el logo como una personalización documentada para el usuario. No se debe sustituir un logo por una copia encontrada sin licencia verificable.

## 5. Arquitectura de los módulos de arquetipo

Añadir un agregado independiente en la raíz para que el código de los adaptadores no dependa de los starters:

```text
archetypes/
├── pom.xml
├── inertia-spring-vue-archetype/
│   ├── pom.xml                         # packaging maven-archetype
│   └── src/main/resources/
│       ├── META-INF/maven/archetype.xml
│       └── archetype-resources/
├── inertia-spring-react-archetype/
│   ├── pom.xml                         # packaging maven-archetype
│   └── src/main/resources/
│       ├── META-INF/maven/archetype.xml
│       └── archetype-resources/
├── inertia-quarkus-vue-archetype/
│   ├── pom.xml                         # packaging maven-archetype
│   └── src/main/resources/
│       ├── META-INF/maven/archetype.xml
│       └── archetype-resources/
└── inertia-quarkus-react-archetype/
    ├── pom.xml                         # packaging maven-archetype
    └── src/main/resources/
        ├── META-INF/maven/archetype.xml
        └── archetype-resources/
```

El POM raíz los incluirá mediante un perfil `archetypes`, activado en CI y release, o como módulos ordinarios si el tiempo de construcción es aceptable. Los artefactos se publicarán por separado; los consumidores no deben descargar ni heredar el repositorio completo.

Cada descriptor `archetype.xml` declarará los conjuntos de archivos, los que requieren filtrado Velocity y los binarios (SVG, favicon y lockfiles) que no deben ser transformados. Maven Archetype usa Velocity para expandir propiedades de las plantillas, por lo que todo literal que contenga sintaxis Maven/JavaScript debe probarse para evitar sustituciones accidentales. [Documentación del plugin Maven Archetype](https://maven.apache.org/archetype/maven-archetype-plugin/plugin-info.html)

### 5.1 Propiedades de generación

| Propiedad | Requerida | Ejemplo | Uso |
|---|---:|---|---|
| `groupId` | Sí | `com.acme` | Coordenada del proyecto generado. |
| `artifactId` | Sí | `hello-inertia` | Directorio y artefacto generado. |
| `version` | Sí | `0.0.1-SNAPSHOT` | Versión inicial de la aplicación. |
| `package` | Sí | `com.acme.hello` | Paquete Java y ruta fuente. |
| `appName` | No | `Hello Inertia` | Título de navegador y prop de bienvenida. |
| `inertiaAdapterVersion` | No | versión publicada actual | Dependencia Spring/Quarkus Inertia. |
| `javaVersion` | No | `21` | `maven.compiler.release`. |
| `frameworkVersion` | No | versión compatible publicada | BOM/parent del framework. |
| `includeGitHubActions` | No | `true` | Incluye un workflow reducido de compilación. |

No exponer `framework` ni `client` como propiedades: cada arquetipo representa una combinación fija y no debe poder generar una salida incoherente. Tampoco incluir una propiedad de versiones arbitraria que produzca combinaciones no probadas; los valores admitidos se documentarán y se validarán durante la generación.

## 6. POM y dependencias de los proyectos generados

### Spring Boot

El POM generado debe usar el parent/BOM de Spring Boot compatible, `spring-boot-starter-web`, la dependencia publicada `spring-inertia`, el plugin de Spring Boot y el starter de pruebas. Debe incluir una propiedad única para la versión del adaptador y evitar importar el POM padre de este repositorio.

### Quarkus

El POM generado debe importar el BOM de Quarkus compatible, incluir REST/Jackson y la dependencia publicada `quarkus-inertia`, además de `quarkus-maven-plugin`, JUnit/RestAssured para pruebas. Debe usar la misma modalidad reactiva que el adaptador, sin mezclar accidentalmente API REST clásica y reactiva.

### Frontend

Los arquetipos React instalarán React, React DOM, `@inertiajs/react`, Vite, el plugin React y TypeScript. Los arquetipos Vue instalarán Vue 3, `@inertiajs/vue3`, Vite, el plugin Vue y TypeScript. Cada `package-lock.json` se generará con una versión LTS definida de Node y se comprobará en CI. Los cuatro clientes resolverán el componente `Welcome` por el mismo nombre que entrega Java.

La capa de presentación se mantendrá equivalente entre Vue y React: mismas props, textos, logos, enlace, estructura semántica, estados de foco y cobertura de prueba. Las diferencias se limitarán al bootstrap y a la sintaxis propia del framework. Así los arquetipos muestran dos integraciones de cliente, no dos productos con comportamientos distintos.

El arquetipo no declarará SSR activado por defecto. SSR añade un proceso de Node y configuración operativa que no es necesaria para validar la primera visita; se ofrecerá como guía posterior. [Renderizado del lado del servidor](https://inertiajs.com/docs/v3/advanced/server-side-rendering)

## 7. Documentación generada para el usuario

Cada proyecto creado incluirá un `README.md` de no más de una página con:

1. Requisitos: JDK, Maven, Node y versión recomendada.
2. Instalación frontend: `npm ci`.
3. Desarrollo en dos terminales: Vite y Spring Boot/Quarkus.
4. Compilación de producción: `npm run build` seguido del empaquetado Maven.
5. URL de la página inicial y la prueba de humo.
6. Enlaces a Inertia, a la documentación específica del adaptador y a reportes de problemas del proyecto generador.
7. Una nota clara: el starter es una base mínima, no una configuración de producción de autenticación, CSRF, seguridad de cabeceras o despliegue.

El README no afirmará que Spring, Quarkus o Inertia patrocinan el arquetipo. Las coordenadas y enlaces deberán quedar parametrizados hasta que el repositorio y Maven Central definitivos estén publicados.

## 8. Pruebas obligatorias

### 8.1 Pruebas dentro de cada proyecto generado

- **Spring:** `MockMvc` verifica que `GET /` sin cabeceras Inertia devuelve HTML, tiene el punto de montaje y serializa la página `Welcome`; una solicitud `X-Inertia: true` devuelve JSON y `X-Inertia: true`.
- **Quarkus:** `@QuarkusTest`/RestAssured verifica los mismos dos contratos, incluyendo la respuesta reactiva.
- **Frontend React:** prueba de tipo/build que asegura que `Welcome.tsx` resuelve las props y que los logos referenciados existen.
- **Frontend Vue:** comprobación de tipos/build que asegura que `Welcome.vue` recibe las mismas props y que los logos referenciados existen.
- **Navegador:** Playwright abre `/`, comprueba el encabezado, el enlace Inertia, el logo de framework correcto y la ausencia de errores de consola.

### 8.2 Pruebas de los arquetipos

Crear integration tests del Maven Archetype Plugin para las cuatro combinaciones:

1. Generar en modo no interactivo con valores de ejemplo seguros.
2. Verificar que no quedan expresiones Velocity ni propiedades `${...}` sin resolver.
3. Ejecutar `mvn test` y `npm ci && npm run build` dentro del proyecto generado.
4. Arrancar la aplicación en un puerto asignado por la prueba, esperar una señal de salud y ejecutar Playwright.
5. Repetir con un `groupId`, `artifactId`, paquete y `appName` que contengan casos habituales para validar el escaping.
6. Confirmar que los arquetipos Spring nunca contienen logo/código Quarkus y viceversa, y que cada arquetipo Vue o React contiene únicamente las dependencias y archivos de su cliente.

Los tests usarán directorios temporales y puertos aleatorios; no deben depender de una aplicación ya ejecutándose en `localhost:8081`.

## 9. CI, catálogo y distribución

1. Añadir un job `archetypes` al workflow principal como matriz de cuatro entradas: `spring-vue`, `spring-react`, `quarkus-vue` y `quarkus-react`. Cada entrada ejecutará validación de descriptor, generación, build Java, build frontend y E2E de su resultado.
2. Subir como artefactos los reportes de generación, JUnit, Vite y Playwright al fallar.
3. Publicar los arquetipos con `packaging` `maven-archetype`, sources, Javadoc cuando aplique, firma y metadatos de licencia/SCM iguales a la release de los adaptadores.
4. Documentar el uso no interactivo con estas cuatro coordenadas definitivas:

| Framework | Cliente | `archetypeArtifactId` |
|---|---|---|
| Spring Boot | Vue 3 | `inertia-spring-vue-archetype` |
| Spring Boot | React | `inertia-spring-react-archetype` |
| Quarkus | Vue 3 | `inertia-quarkus-vue-archetype` |
| Quarkus | React | `inertia-quarkus-react-archetype` |

Por ejemplo, para Spring Boot + Vue:

```text
mvn archetype:generate -B \
  -DarchetypeGroupId=<publisher-groupId> \
  -DarchetypeArtifactId=inertia-spring-vue-archetype \
  -DarchetypeVersion=<released-version> \
  -DgroupId=com.example \
  -DartifactId=hello-inertia \
  -Dpackage=com.example.hello
```

5. Crear un catálogo Maven opcional cuando los cuatro arquetipos estén en un repositorio público. El catálogo es una comodidad; el comando con coordenadas completas es el camino documentado y reproducible.
6. Versionar cada arquetipo de forma independiente si el contenido del starter cambia sin modificar el adaptador, pero mantener una tabla explícita de compatibilidad arquetipo–adaptador–framework–cliente.

## 10. Fases de implementación

| Fase | Entregable | Criterio de aceptación |
|---:|---|---|
| 0 | Decisión de marca y versiones compatibles | Aprobación de uso de SVG y tabla de versiones. |
| 1 | Cuatro fixtures mínimos (Spring+Vue, Spring+React, Quarkus+Vue y Quarkus+React) | Cada uno se ejecuta manualmente desde un directorio limpio. |
| 2 | Cuatro módulos `maven-archetype` y descriptores | `archetype:generate` produce las cuatro combinaciones sin tokens sin resolver. |
| 3 | Landing `Welcome`, logos y accesibilidad | Cada página muestra Inertia, solo el framework elegido y el cliente elegido; Lighthouse/Playwright básico verde. |
| 4 | Pruebas contractuales de controller y frontend | HTML inicial y respuesta Inertia JSON pasan en Spring/Quarkus y Vue/React. |
| 5 | IT de generación y CI | Generación, build y navegador verdes en Linux desde un clon limpio. |
| 6 | README, catálogo y publicación candidata | Un usuario externo genera, ejecuta y entiende los cuatro starters. |
| 7 | Release | Artefactos firmados publicados y documentación con coordenadas reales. |

## 11. Definición de terminado

- [x] Existen cuatro artefactos Maven distintos y versionados: Spring+Vue, Spring+React, Quarkus+Vue y Quarkus+React.
- [x] Cada uno genera una aplicación válida con controller Java que usa el adaptador Inertia.
- [x] `GET /` produce HTML Inertia inicial; una visita Inertia produce el page object JSON correcto.
- [x] Cada página muestra “I ♥ Inertia.js”, enlace seguro a Inertia, logos oficiales de Java, Inertia, Spring o Quarkus y Vue o React según la combinación.
- [x] Los logos tienen licencia/uso de marca verificado y atribución cuando corresponda.
- [x] Los cuatro proyectos generados construyen Java y frontend desde cero y pasan pruebas de backend en local (`mvn test` verde 2026-09-06 en las 4 combinaciones).
- [x] No quedan dependencias a rutas locales, artefactos `SNAPSHOT`, secretos ni servicios externos en el proyecto generado.
- [x] La guía de generación usa coordenadas reales (`io.github.dg`, versión `0.0.1`) y versiones compatibles documentadas (ver §0).

> Nota de ejecución 2026-09-07: logos oficiales de Java, Inertia.js, Spring,
> Quarkus, Vue.js y React (Devicon + Simple Icons, SVGs locales) con atribución y
> titulares en `THIRD_PARTY_NOTICES.md` y política de retirada a petición del titular.
> Hallazgos corregidos: job `archetypes`
> en CI (matriz x4 con `mvn test` del starter), job E2E matricial Spring/Quarkus con
> health-wait y teardown garantizado, suite contractual Playwright común
> (`e2e/inertia-contracts.spec.ts`, 5/5 en vivo en ambos demos), verificación de la
> matriz de conformidad en CI y bug de props nulas en partial reload corregido en
> ambos adaptadores con regresión espejo.
> Nota de ejecución nativo/Docker 2026-09-07 (autorización expresa, builder `jdk-25`):
> perfiles `native` añadidos a los POM generados, `Dockerfile`+`.dockerignore`+Maven
> Wrapper en los 4 starters, README con sección nativa. Validado de punta a punta con
> `docker build`+`docker run`: Quarkus 200 HTML/JSON en `/`; Spring requirió además
> corregir `InertiaRuntimeHints` (faltaban categorías `INTROSPECT_*`, `ScrollProp` y
> `@ImportRuntimeHints` en la autoconfiguración —sin ello el nativo fallaba con
> `Record components not available for PageObject`— con test `InertiaRuntimeHintsTest`);
> tras el fix, Spring nativo 200 HTML/JSON en `/`. Límite conocido: el build Docker
> exige adaptadores publicados en Central (hoy solo `.m2` local).
> Prueba visual Quarkus 2026-09-07: starter `inertia-quarkus-vue` generado, imagen
> nativa construida (14m24s) y contenedor sirviendo HTML/JS/CSS/SVGs con 200;
> pantallazo Playwright del Welcome con los 4 logos oficiales verificado.
> Hallazgos de assets corregidos en plantillas: Quarkus sirve estáticos desde
> `META-INF/resources` (brand movido allí), `outDir` Vite a
> `../../main/resources/...`, entry renombrado a `app.*` (la plantilla pide
> `assets/app.js`) y `emptyOutDir: false` (no borra los SVG comiteados).

## 12. Riesgos que deben controlarse

| Riesgo | Mitigación |
|---|---|
| Un template tiene sintaxis Velocity interpretada por error | IT de generación y revisión de archivos filtrados/binarios. |
| El controller devuelve HTML literal y no usa el protocolo | Pruebas separadas para petición inicial HTML y petición `X-Inertia`. |
| Spring, Quarkus, Vue y React divergen | Mantener una especificación común de UX y una suite contractual con los mismos escenarios para las cuatro combinaciones. |
| Los logos violan una guía de marca | Aprobación previa, recursos oficiales locales y atribución; fallback a texto enlazado. |
| Las versiones generadas dejan de ser compatibles | Tabla de compatibilidad, Dependabot y CI de generación con cada release. |
| El starter parece una aplicación de producción | README con límites explícitos y sin credenciales/configuración de ejemplo inseguras. |
