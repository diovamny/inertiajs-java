# Auditoría release-0.0.5 — M1 Reproducibilidad y evidencia (P100-02/03)

- Rama: `release-0.0.5`. Fecha: 2026-09-23.
- Resultado: **VERDE — puede continuar a M2/M3/M5.**

## Criterios 100% M1

- [x] Maven Wrapper presente y funcional: `mvnw`, `mvnw.cmd`,
  `.mvn/wrapper/maven-wrapper.properties` (Maven 3.9.11, wrapper 3.3.2).
  Verificado: `.\mvnw.cmd -version` → Maven 3.9.11, Java 21.0.2 en Windows.
  CI ya no depende de Maven global (`ci.yml`, `release.yml`,
  `native-tests.yml` usan `./mvnw`).
- [x] Versiones bloqueadas: Enforcer (`requireJavaVersion [21,)`,
  `requireMavenVersion [3.9,)`) verde en `validate`; `package.json engines`
  Node 24.21.0 + npm 11.17.0; `.nvmrc` 24.21.0; `check-versions.mjs` OK 0.0.5.
- [x] Comandos únicos documentados (`CONTRIBUTING.md`): `npm ci`,
  `npm run verify:metadata`, `./mvnw -B clean test`,
  `./mvnw -B verify -Pquality-gates`, `npm run test:e2e`.
- [x] Evidencia trazable: `specs/e2e-compliance.yaml` (10 escenarios, 9 celdas
  v3-only); job `clean-checkout` (ubuntu+windows) con artefactos
  Surefire/Failsafe; `verify:metadata` = versions + generate + conformance.
- [x] Suite limpia verde con wrapper en Windows:
  `.\mvnw.cmd -B clean test` → BUILD SUCCESS (core, tck, quarkus 320 tests,
  spring, security; 0 failures/errors). Logs de error esperados
  (ErrorTestResource 400/500, PageTooLarge 413, SSR unreachable) son
  aserciones negativas, no fallos.

## Archivos M1

Wrapper (3), `.nvmrc`, `specs/e2e-compliance.yaml`, `pom.xml` (enforcer),
`package.json` (engines + `verify:metadata`/`verify:all`), `CONTRIBUTING.md`,
`.github/workflows/{ci,release,native-tests}.yml` (wrapper + 0.0.5 + job
clean-checkout).

## Límites

- Linux clean-clone se verifica en CI (`clean-checkout` ubuntu-latest);
  local solo Windows en esta estación.
- `verify -Pquality-gates` completo y E2E 9 celdas corren en M4/M8.
