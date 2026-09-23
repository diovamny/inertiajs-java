# Auditoría release-0.0.5 — M4a E2E con clientes oficiales, parte 1 (P100-08/09)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE para el alcance M4a (Vue × Spring MVC 10/10). M4b
  (resto de celdas) sigue en esta misma rama antes del tag.**

## Alcance M4a (probado, verde)

Suite `e2e/inertia-contracts.spec.ts` (Chromium, cliente oficial
`@inertiajs/vue3` v3 + Vue 3) contra `spring-kitchen-sink-0.0.5.jar`:
**10/10 verde**, cada prueba con mount real, respuesta relevante, estado
DOM y cero errores de consola (`pageerror` + `console[type=error]`):

- Bootstrap v3 (script JSON único), visita JSON, parcial `only`, 409 con
  `X-Inertia-Location` (preexistentes, siguen verdes).
- M2: formulario vacío → `Please enter your full name.` +
  `We need your email address.` + cabecera `form.errors` visibles.
- M2: secondary con `errorBag` → `secondaryForm.errors` visible y el
  primario limpio (sin sangrado entre bags).
- M3: `Add Notification` → badge `1 total` → `2 total` (append observado
  por el cliente, no solo metadata).
- PROTO-057B: `Visit with Callback` → placeholder `Navigating from
  source (was: "This is the source page.")` visible en <1.5s (el sidecar
  duerme 2s: solo el cliente pudo pintarlo) → luego `Hello from the
  server!`. Primera prueba observada por cliente oficial de instant visits.
- Upload multipart real (`avatar.png` vía `input[type=file]`) → `onSuccess`
  resetea el form (`Choose image...` de vuelta).
- Login mount sin errores (testigo).

## M4b pendiente (misma rama 0.0.5, bloqueantes del RC)

1. **Quarkus-Vue cells**: el login por navegador no completa sobre http.
   Causas en la demo (no en el adaptador): `%prod.cookie-force-secure=true`
   impide la cookie XSRF sin https (mitigado en CI con
   `QUARKUS_REST_CSRF_COOKIE_FORCE_SECURE=false`, ya añadido) y una
   anomalía post-login por diagnosticar (página en blanco + `400` en un
   Inertia GET posterior; el login por API llega a `303`). El TCK prueba el
   protocolo Quarkus; falta la observación cliente.
2. **React cells**: `spring-pingcrm-react` no tiene `webui` (solo el pom
   lo referencia); Quarkus `pingcrm-react` sí tiene. Hay que crear el
   frontend Spring-React o una fixture mínima.
3. **Svelte cells**: existen `pingcrm-svelte` en ambos; falta cablear la
   matriz CI.
4. **Reactive cells**: la demo Quarkus sirve navegación por Reactive Routes
   (¡las celdas Quarkus-Vue ya pisan Reactive!), pero falta celda E2E
   dedicada + SSR E2E (E2E-09 `NO_EVALUADO`) + negativas por familia.

## Evidencia

- `spring-kitchen-sink-0.0.5.jar` + `npx playwright test` → `10 passed`.
- `specs/e2e-compliance.yaml` con `verified_cells` honestas por escenario.
- Matriz: `62 rows, 60/62 verified, 8 E2E` (053B/054B con `+E2E spring-mvc`).
