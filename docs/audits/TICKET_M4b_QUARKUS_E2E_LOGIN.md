# Ticket M4b-01 — E2E Quarkus-Vue: el login por navegador no completa sobre http (demo, pre-tag 0.0.5)

- Estado: abierto, bloquea las celdas E2E Quarkus (no bloquea TCK/contrato).
- Alcance: `examples/quarkus/kitchen-sink` (jar prod) + `@inertiajs/vue3`.
  El adaptador está descartado como causa (ver evidencia).

## Repro (100%)

1. `INERTIA_CSRF_HMAC_KEY=<32+>` + `QUARKUS_REST_CSRF_COOKIE_FORCE_SECURE=false`
   (http necesita esto: `%prod.cookie-force-secure=true` impide la cookie).
2. `java -jar examples/quarkus/kitchen-sink/target/quarkus-app/quarkus-run.jar`.
3. Chromium: `GET /login` → 200, mount limpio; rellenar `password`,
   click `Log in` → `POST /login` → **303 `Location: http://…/login`**
   → `GET /login` (Inertia) → **400 vacío, sin content-type** → `#app`
   en blanco. `props.errors` vacío, usuario nulo.

## Datos que descartan al adaptador

- Ningún path del adaptador emite 400 vacío sin content-type
  (`InertiaVertxHandler:251` y mappers llevan entidad JSON; `ErrorResponseFactory`
  siempre serializa `ErrorPage`). El 400 viene de fuera del adaptador
  (rest-csrf/Quarkus HTTP/demo).
- Tras el POST, `GET /login` plano → 200 e Inertia GET posterior → 200 con
  `errors:{}`: el 400 es transitorio tras el POST (sesión recién escrita).
- Contraste: credenciales erróneas tampoco muestran flash (ni siquiera
  `These credentials…`), luego el flash de la sesión tampoco sobrevive —
  apunta a sesión/CSRF, no a validación.
- En `@QuarkusTest` el mismo login (sin `X-Inertia`) funciona
  (`PrecognitionQuarkusTest.login` → 303): diferencia prod-vs-test por aislar
  (perfil, Secure, HMAC, store de sesión).

## Hipótesis ordenadas

1. `quarkus-rest-csrf` rechaza el token en prod (firma/sesión/rotación) y el
   bridge lo convierte a 303; el 400 posterior es del propio rest-csrf u otro
   filtro ante la sesión marcada.
2. Rotación/pérdida de sesión Vert.x entre POST y GET (flash + validación
   posteriores se pierden).
3. Interacción `quarkus.http.auth` (permit/secured) con rest-csrf en el jar prod.

## Criterio de cierre (M4b)

- Login por navegador verde en Quarkus (misma suite `inertia-contracts`),
  sin cambiar el contrato del adaptador; si el fix toca la demo, documentarlo
  en la demo (no en el adaptador) + nota en `docs/reactive-parity.md`/CI.
- No se marca ninguna celda Quarkus E2E_VERIFICADO hasta entonces
  (`specs/e2e-compliance.yaml` ya refleja `verified_cells` honestas).

## Otros M4b (mismo ticket padre, pre-tag)

- M4b-02 React: `spring-pingcrm-react` sin `webui` (el pom lo referencia);
  crear frontend o fixture mínima React v3.
- M4b-03 Svelte/SSR/E2E-09/negativas: cablear matriz CI 9 celdas + sidecar.
