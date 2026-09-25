# Playwright + OpenCode MCP

Versión de `@playwright/test`: **^1.63.0** (estable, 4 sep 2026; `latest` en npm al
momento de la actualización). Release aditiva: ninguna API de test existente cambia.
`playwright` / `playwright-core` van alineados a la misma versión y los navegadores se
reinstalaron (Chromium 153 / chromium-1243, Firefox 155, WebKit 26.6).

> Nota: el prompt original de esta tarea mencionaba "por qué se mantiene en 1.62.1" —
> texto desactualizado. Se actualizó a **1.63.0** según lo pedido y se validó sin regresiones.

## Los dos MCP (conviven, no se pisan)

| MCP | Paquete | Rol |
|-----|---------|-----|
| `playwright-test` | `npx playwright run-test-mcp-server` (viene con `@playwright/test`) | Planificar / generar / ejecutar / curar tests |
| `playwright` | `npx -y @playwright/mcp@0.0.82` (fijado, no `@latest`, por reproducibilidad) | Navegación real: UI, DOM, consola, red, screenshots |

`@playwright/mcp` aún se versiona `0.0.x` y evoluciona rápido; por eso se fijó la `0.0.82`
(última estable verificada en npm). Para subirla, verificar antes en
`npmjs.com/package/@playwright/mcp` y re-listar sus tools (varían entre releases).

## Qué agente usa cada uno

- `playwright-test-generator` → MCP `playwright-test` (crea tests).
- `playwright-test-healer` → MCP `playwright-test` (depurar/reparar tests).
- `playwright-test-planner` → MCP `playwright-test` (planes de prueba).
- `playwright-auditor` (nuevo) → MCP `playwright` (exploración y evidencia; **no** genera
  ni edita tests — no tiene tools `write`/`edit`).

Política de tools: mínimo privilegio. `opencode.json` bloquea todo por defecto
(`"playwright*": false`) y cada agente habilita solo lo que necesita. Los 12 tools del
auditor se verificaron contra la lista real que expone `@playwright/mcp@0.0.82`
(`tools/list` vía protocolo MCP, 25 tools en total).

## Cómo correr tests

```bash
npx playwright test --list   # debe listar 34 tests en 4 archivos
npx playwright test          # requiere el backend en http://localhost:8082
```

Sin backend, los 34 tests fallan con `net::ERR_CONNECTION_REFUSED` (línea base conocida,
no una regresión). Un spec apunta a `:8081` — detalle preexistente.

## Cómo correr una auditoría

Pedir al agente `playwright-auditor` la URL/flujo a auditar con el backend levantado.
Flujo: Detectar → Reproducir → Confirmar → Capturar evidencia → Documentar. Cada hallazgo
lleva ID, severidad, URL, pasos, esperado vs. observado, consola/red, snapshot y screenshot.

Notas de entorno (verificadas 2026-09-25):

- El MCP `playwright` corre **headed por defecto**; en entornos sin display usar `--headless`.
- Trae su propio `playwright-core` (en 0.0.82 es un `1.64.0-alpha`) con sus propios
  navegadores (`chromium-1246`, `firefox-1549`, …): instalarlos con
  `npx -y @playwright/mcp@0.0.82 install-browser` (distintos de los del proyecto).
- El perfil persistente es exclusivo por cliente: para sesiones paralelas usar `--isolated`.

## Si ambos MCP entran en conflicto

1. Verificar nombres en `opencode.json`: deben ser `playwright-test` y `playwright`
   (nunca fusionarlos: son test-runner vs. navegador para agentes).
2. Si una tool no existe, re-listar con `tools/list` — no inventar nombres.
3. Si el navegador no abre pestañas / la navegación se aborta, revisar flags
   (`--headless`, `--isolated`) y que los navegadores del MCP estén instalados.
4. No tocar los agentes `generator`/`healer`/`planner` al diagnosticar el MCP nuevo.
