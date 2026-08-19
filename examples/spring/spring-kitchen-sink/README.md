# Spring Boot Kitchen Sink Demo

Port 1:1 del showcase [Quarkus `kitchen-sink`](../quarkus/kitchen-sink/) construido
con **Spring Boot 4.1 + [spring-inertia](../../../spring-inertia/)**. Frontend
Vue 3 igual al de Quarkus (misma fuente `src/main/webui`), con build real de
Vite + Tailwind 4.

## Características

- **Autenticación por sesión** con login/logout y rutas protegidas (interceptor
  + `AuthService`), redirecciones 302/303 y `X-Inertia-Location` para clients
  Inertia.
- **Páginas de error como componente Inertia**: `ErrorPage` con status HTTP
  semántico (400/403/404/409/422/500) vía `ErrorMapper` por visita.
- **Precognition**: validación incremental con headers `Precognition` /
  `Precognition-Validate-Only` (204/422 + `Vary: Precognition`).
- **Data loading**: deferred props, partial reloads, infinite scroll (cursor),
  polling, prop merging (MERGE/PREPEND con `matchOn`), once props (con TTL y
  custom key), optional props.
- **Formularios**: uso de formularios con `useForm`, validation de objetos y
  error bags, file uploads múltiples (límite de 5).
- **Estado**: flash messages, shared props, scroll props.
- **HTTP**: uso de `fetch` para endpoints JSON propios.
- **Layout / navegación / redes**: layout persistente, links, prefetching,
  network errors (online/offline).
- **CRM**: dashboard, contacts/organizations con paginación y búsqueda, notas.
- **Migraciones Flyway** (V1-V4) + base H2 (archivo en producción, in-memory en
  tests), seed de datos demo (15 organizaciones, 15 contactos, notas, 1 user).

## Run

```bash
# desde la raíz del repo (construye primero los adaptadores)
mvn clean install -DskipTests -pl spring-inertia -am

# build + test del demo
mvn -f examples/spring/spring-kitchen-sink/pom.xml clean test

# run (http://localhost:8080)
mvn -f examples/spring/spring-kitchen-sink/pom.xml spring-boot:run
```

El primer arranque crea la base H2 en `data/spring-kitchen-sink.mv.db`, ejecuta
las migraciones Flyway y seeda los datos demo.

### Credenciales

| Email | Password |
| --- | --- |
| `test@example.com` | `password` |

> Nota: como en el demo Quarkus, el login no verifica la contraseña: solo
> requiere un email existente y una contraseña no vacía.

## Frontend

```bash
cd examples/spring/spring-kitchen-sink/src/main/webui
npm install
npm run dev      # servidor Vite en http://localhost:5173
```

El `frontend-maven-plugin` descarga Node 22 y ejecuta `npm install` + `vite build`
en el build de Maven; el bundle compilado queda commiteado en
`src/main/resources/static`, así el demo también compila y corre sin npm.

## CSRF

Deshabilitado (`inertia.csrf-enabled=false`), igual que los demás demos: el
cliente de fetch de Inertia v3 no envía `X-XSRF-TOKEN` automáticamente. Ver el
[README de spring-inertia](../../../spring-inertia/README.md) para el soporte
CSRF por cookie del adaptador.

## Tests

`KitchenSinkSpringTest` porta la suite del demo Quarkus (Precognition) y añade
smokes de las 5 mejoras de spring-inertia: 23 escenarios MockMvc — auth,
precognition, CRM paginado/búsqueda, error pages, merge/once/deferred/partial
props, file uploads, http API y logout. Usa H2 in-memory seedado por JVM.

```bash
mvn -f examples/spring/spring-kitchen-sink/pom.xml test
```