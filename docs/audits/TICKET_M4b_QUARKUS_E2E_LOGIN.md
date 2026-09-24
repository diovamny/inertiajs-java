# Ticket M4b-01 — E2E Quarkus-Vue: login bloqueado por firma CSRF (CERRADO)

- Estado: **cerrado 2026-09-24** (causa raíz encontrada y corregida).
- Alcance: `examples/quarkus/*` (jar prod) + clientes oficiales cookie-echo.

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

## Cierre (causa raíz confirmada por bytecode)

`CsrfRequestResponseReactiveFilter.verifyCsrfToken` exige
`cookie.equals(sign(header))`: con `token-signature-key` la cookie contiene
la FIRMA, y ningún cliente que solo reenvíe la cookie (todos los oficiales
Inertia) puede pasar jamás. El bridge exigía la clave en prod (`fail-fast`
≥32) y las 4 demos la fijaban → login imposible en prod; en `@QuarkusTest`
(sin clave, doble-submit plano) funcionaba. El adaptador queda exonerado.

Fix aplicado: bridge avisa en vez de exigir (solo flujos server-rendered
con token crudo pueden firmar); 4 demos sin clave + nota; CI sin HMAC;
`security-integration-plan.md` corregido.

Hallazgo colateral (demo, también corregido): `ValidationRequest.age`
`Integer` revienta JSON-B con `""` (Jackson coacciona a null) → 500 en el
submit primario; parseo explícito con los mismos mensajes.

## Otros M4b (mismo ticket padre, pre-tag)

- M4b-02 React: `spring-pingcrm-react` sin `webui` (el pom lo referencia);
  crear frontend o fixture mínima React v3.
- M4b-03 Svelte/SSR/E2E-09/negativas: cablear matriz CI 9 celdas + sidecar.
