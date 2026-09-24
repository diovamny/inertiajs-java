# Auditoría release-0.0.5 — FD M4b E2E multicliente (P100-08/09)

- Rama: `release-0.0.5`. Fecha: 2026-09-24.
- Resultado: **VERDE parcial honesto: I100 37/90.** Ninguna celda marcada sin
  corrida verde en esta rama.

## Evidencia ejecutada (navegador real, Chromium, cero errores consola)

- kitchen-sink Vue × Spring MVC y Quarkus REST: `inertia-contracts.spec.ts`
  **11/11** ambos (bootstrap, visita, parcial, 409, validación M2, bags M2,
  append M3, instant-visits PROTO-057B, redirects E2E-07, upload, mount).
  En Quarkus, instant-visits y redirects corren sobre Reactive Routes
  (`@Route`) → celdas `vue/quarkus-reactive` verificadas (E2E-06, E2E-07).
- PingCRM × 6 combinaciones (Vue/React/Svelte × Spring/Quarkus):
  `pingcrm-contracts.spec.ts` **5/5** en las 6 (shell, JSON, mount,
  wrong-creds, login→dashboard).
- SSR E2E-09: starters Vue generados (Spring + Quarkus) + sidecar real —
  render sin-JS (prueba servidor), hidratación limpia, fallback con sidecar
  caído. Celdas `vue/spring-mvc`, `vue/quarkus-rest` verificadas.

## Fixes con evidencia (nada diferido)

- M4b-01 (CERRADO): firma CSRF rompía todo login Quarkus prod → bridge
  avisa en vez de exigir; 4 demos sin clave; CI sin HMAC. Causa probada por
  bytecode + antes/después verde.
- Quarkus demo `ValidationRequest.age` Integer → 500 con JSON-B (`""`) →
  parseo explícito con mismos mensajes (Spring/Jackson coaccionaba).
- Spring React: sin seeder (login imposible) → `DataSeeder` determinista;
  sin `DashboardController` (`GET /` 404) → portado; `authProps` sin
  `account` (TypeError en Layout) → añadido; sin BOM (ByteBuddy ausente en
  boot) → `spring-boot-dependencies`; sin `starter-flyway` (Flyway no corría).
- Spring starter template sin `repackage` (Fase B, ya corregido).

## I100 real: 37/90

`specs/e2e-compliance.yaml` con `verified_cells` por escenario; el generador
calcula I100 solo de ahí y falla ante `E2E_VERIFICADO` sin celdas enlazadas
o celdas fuera de su escenario. Resto (React/Svelte kitchen-sink-level,
deferred/once/rescue, SSR React/Svelte, E2E-02/03 multicliente) queda
PARCIAL/NO_EVALUADO explícito — próxima oleada en esta misma rama.
