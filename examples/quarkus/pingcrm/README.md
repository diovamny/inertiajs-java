# PingCRM (Quarkus port)

Réplica fiel del demo oficial [inertiajs/pingcrm](https://github.com/inertiajs/pingcrm)
(Laravel + Vue 3) construida sobre **Quarkus 3.38** y la librería local
**quarkus-inertia** (`io.github.diovamny.quarkus.inertia:quarkus-inertia:0.0.1`).

## Requisitos

- JDK 21
- Maven 3.9+
- Node 20.11+ / npm (solo para reconstruir el frontend)

## Credenciales demo

| Usuario             | Password | Rol   |
| ------------------- | -------- | ----- |
| johndoe@example.com | secret   | owner |

## Comandos

```bash
# 1. Instalar la librería quarkus-inertia (raíz del repo)
mvn -q install -DskipTests

# 2. Frontend (solo necesario si cambias los fuentes de webui/)
cd src/main/webui
npm install
npm run build        # escribe en src/main/resources/META-INF/resources/assets

# 3. Backend: test + build
mvn verify           # 25 tests (QuarkusTest + REST Assured)

# 4. Ejecutar
mvn quarkus:dev      # http://localhost:8080
# o
java -jar target/quarkus-app/quarkus-run.jar
```

El primer arranque crea la BD H2 en `data/pingcrm.mv.db` y la rellena con el
seed demo (cuenta "Acme Corporation", John Doe, 100 organizaciones y 100
contactos deterministas con Faker seed 42).

## Arquitectura

- **API reactiva**: controllers devuelven `Uni<Object>` con `@Blocking`
  (obligatorio: el `AuthFilter` hace consultas de sesión y la BD es H2, sin
  soporte reactivo).
- **Persistencia**: Hibernate ORM + Panache (imperativo) con repos propios,
  migraciones Flyway (`db/migration/V1..V5`), generadores de secuencia
  `allocationSize=1` y naming físico snake_case.
- **Sesión**: Vert.x (`vertx-web.session`) con `SessionConfig`; el `AuthFilter`
  protege rutas (públicas: `assets`, `img`, `build`, `login`, `logout`) y
  comparte `auth` en cada request.
- **Imágenes**: `ImagesController` reemplaza a League Glide (resize/crop
  server-side con `?w&h&fit=crop`, anti path-traversal).
- **Frontend**: Vue 3 + Tailwind 4 en `src/main/webui` (port completo de los
  fuentes oficiales adaptado a Inertia v3: `useForm`, `router.*`, composable
  `useSyncErrors` para errores flash).

## Decisiones de port

- **H2 en lugar de MySQL**: el demo oficial usa MySQL; aquí H2 archivo
  (`jdbc:h2:file:./data/pingcrm`) para cero dependencias externas.
- **CSRF desactivado** (`inertia.csrf-enabled=false`): el cliente fetch de
  Inertia v3 no envía `X-XSRF-TOKEN` automáticamente; el demo de la librería
  (`examples/quarkus/demo-app`) sí usa CSRF vía cookies de doble envío.
- **Validación**: los errores de validación se redirigen de vuelta con flash
  `errors` (303 en non-GET, 302 en GET), reproducido por
  `PrecognitionExceptionMapper`.
- **Soft delete**: los `update`/`softDelete`/`restore` usan `merge` en vez de
  `persist` porque las entidades llegan detached (read sin transacción).

## Estructura

```
src/main/java/com/example/pingcrm/
  controller/   Auth, Dashboard, Organizations, Contacts, Users, Reports, Images
  service/      AuthService, OrganizationService, ContactService, UserService, Pagination
  repository/   repos Panache
  entity/       Account, User, Organization, Contact
  dto/          LoginForm, OrganizationForm, ContactForm, UserForm, FormValidator
  config/       AuthFilter, SessionConfig
  seed/         DataSeeder
src/main/webui/            frontend Vue (port)
src/test/java/  PingCrmTest (25 tests)
```
