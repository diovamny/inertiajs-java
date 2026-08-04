# Plan PingCRM (Quarkus) — estado final

Objetivo: port del demo [inertiajs/pingcrm](https://github.com/inertiajs/pingcrm)
sobre Quarkus 3.38 + librería local `quarkus-inertia` 0.0.1, con `mvn verify`
verde y documentación.

## Tareas ejecutadas

| # | Tarea | Estado |
|---|-------|--------|
| 1 | Descargar fuentes oficiales (39 archivos, temp) | Hecho |
| 2 | Scaffold Maven + `application.properties` + `templates/index.html` | Hecho |
| 3 | Migraciones Flyway V1–V5 | Hecho (V1–V4 con secuencias) |
| 4 | Entidades + repos Panache | Hecho |
| 5 | Services (Auth/Org/Contact/User/Pagination) | Hecho |
| 6 | DTOs + validación + 7 controllers | Hecho |
| 7 | `AuthFilter` + shared props + `SessionConfig` | Hecho |
| 8 | `DataSeeder` (Faker seed 42, 100 orgs / 100 contacts) | Hecho |
| 9 | Port frontend a `src/main/webui` + build Vite | Hecho |
| 10 | `PingCrmTest` (25 tests ordenados) | Hecho — 25/25 verdes |
| 11 | `mvn verify` (core 179 + demo-app 88 + pingcrm 25) | Hecho — todo verde |
| 12 | Smoke test jar empaquetado + persistencia H2 file | Hecho |
| 13 | README + este plan | Hecho |

## Problemas resueltos (debug real con `mvn verify`)

1. **Panache + `@Id` propio**: Quarkus rechaza redefinir `id` de `PanacheEntity`
   → entidades extienden `PanacheEntityBase` con `@Id` + `@SequenceGenerator`.
2. **IDENTITY vs secuencias**: Hibernate 7 esperaba `<tabla>_seq`
   → migraciones con `CREATE SEQUENCE` + `allocationSize=1`.
3. **Naming snake_case**: `CamelCaseToUnderscoresNamingStrategy` en main y test.
4. **Seeder vs Flyway**: sin orden entre observers de `StartupEvent`
   → `flyway.migrate()` explícito e idempotente al inicio del seed.
5. **Bloqueos en hilo IO**: `AuthFilter` consulta BD en `authProps()` y las
   rutas non-`@Blocking` 500/530 → `@Blocking` a nivel de clase en todos los
   controllers.
6. **`Map.of` con valores null**: `Map.of("search", search, ...)` NPE
   → `LinkedHashMap` en services y `editData`.
7. **`persist()` sobre entidades detached** (read sin tx) → `merge()` en
   update/softDelete/restore.
8. **`emailExistsForOtherUser`**: `id <> null` nunca matchea
   → `(?2 is null or id <> ?2)`.
9. **REST Assured sigue redirects** → `followRedirects(false)` global.
10. **Validación Inertia devolvía 302 en POST** → `PrecognitionExceptionMapper`
    devuelve 303 para non-GET (paridad con `RedirectProcessor`); test del core
    actualizado en consecuencia.
11. **Cookie de sesión**: Vert.x solo reenvía `Set-Cookie` si la sesión cambió
    → `captureSession` tolerante (no sobrescribe con null).

## Decisiones clave

- H2 file (`./data/pingcrm`) en lugar de MySQL.
- `inertia.csrf-enabled=false` (fetch v3 no envía `X-XSRF-TOKEN`); demo-app
  sigue cubriendo el caso CSRF real.
- Flash de errores: servidor `inertia.flash("errors", ...)` + redirect;
  frontend `useSyncErrors` los fusiona en `useForm`.
- Users index sin paginación (paridad con el `->get()` oficial); contacts sin
  filtro `organization_id`.
- Imágenes sin League Glide: `ImagesController` con resize/crop propio.
- Demo user (`johndoe@example.com`) protegido contra edición/borrado.
- Update de users como `POST /{id}` multipart con `_method=put` (convención
  Laravel).

## Próximos (opcional)

- Probar flujo HMR con `vite dev` + `mvn quarkus:dev`.
- README raíz del repo con el estado de los tres módulos.
