# Plan: Ping CRM React (`examples/pingcrm-react`)

## Objetivo
Nuevo demo con **el mismo backend Quarkus** de `examples/pingcrm` (copia exacta, protocolo agnóstico del framework) y un frontend **React 19 + TypeScript + Inertia v3 (`@inertiajs/react`)**, con **la misma UI visual** que el demo Vue actual, **SSR completo**, README y tutorial corto. Referencia: el port oficial `Landish/pingcrm-react`, actualizado a Inertia v3.

## Decisiones confirmadas
| Tema | Elección |
|---|---|
| Lenguaje | TypeScript (como el port React oficial) |
| UI | Idéntica al demo Vue actual (mismas props → comparación limpia) |
| Docs | README + `PingCRM-React-Tutorial.md` corto |
| SSR | Completo (server Node + `inertia.ssr-enabled=true`) |

## 1. Estructura del nuevo módulo
```
examples/pingcrm-react/
├── pom.xml                        ← copia de pingcrm; artifactId pingcrm-react; frontend-maven-plugin igual
├── README.md  +  PingCRM-React-Tutorial.md
├── src/main/resources/
│   ├── application.properties     ← igual + inertia.ssr-enabled=true, ssr-url=http://localhost:13714
│   ├── db/migration/V1..V5.sql    ← copiados verbatim
│   ├── templates/index.html       ← el MISMO shell Qute (ya soporta {ssrHead}/{ssrBody})
│   └── META-INF/resources/assets/ ← build Vite (gitignored)
├── src/main/java/com/example/pingcrm/…   ← COPIADO IDÉNTICO (mismo package, sin renombrar)
├── src/test/java/…/PingCrmTest.java      ← copiado idéntico (25 tests)
└── src/main/webui/                       ← la parte NUEVA (React + TS)
    ├── package.json / tsconfig.json / vite.config.ts
    └── resources/js/
        ├── app.tsx              ← createInertiaApp; hydrateRoot si data-server-rendered
        ├── ssr.tsx              ← createServer de @inertiajs/react + renderToString
        ├── utils.ts             ← useSyncErrors hook, pickBy, helpers
        ├── types/index.d.ts     ← PageProps: Auth, Organization, Contact, User, Paginated
        ├── Layouts/MainLayout.tsx
        ├── Components/          ← Inputs, LoadingButton, Dropdown, Logo, Icon, MainMenu,
        │                          SearchFilter, Pagination, FlashMessages, TrashedMessage
        └── Pages/               ← Auth/Login, Dashboard/Index, Organizations/{Index,Create,Edit},
                                     Contacts/{…}, Users/{…}, Reports/Index  (mismo naming que hoy)
```

## 2. Backend: copia sin cambios
- Copiar `src/main/java`, `db/migration`, `application.properties` (agregando solo `inertia.ssr-enabled=true`), `templates/index.html` y `PingCrmTest.java` verbatim. Los 25 tests REST Assured validan el **PageObject JSON**, no el framework client → pasan igual.
- Verificar con `mvn verify` antes de tocar el frontend (base verde).

## 3. Frontend React — mapeo Vue → React (misma UI)
- **Forms**: `useForm` hook (`form.data`, `form.setData`, `form.submit('post'|'put'|'delete', url)`, `form.errors`, `form.processing`) → reemplaza `this.form.post(...)`.
- **v-model** → inputs controlados (`value` + `onChange`); los componentes Shared reciben `value/onChange/error` igual que hoy reciben `modelValue/error`.
- **Layout persistente**: `layout: Layout` (opción Vue) → `<InertiaLayout>` (componente v3 de React) o wrapper manual en `MainLayout.tsx` (patrón del port Landish).
- **Estado de página**: `$page.props` → `usePage()`; `router.get/post/put/delete` y `<Link>` idénticos en API.
- **Errores flash del server**: `useSyncErrors(form)` portado a un hook React (`useEffect` + `usePage().props.errors` → `form.setError`), mismo contrato `inertia.flash("errors", …)` del backend.
- **Live search**: `pickBy` + `throttle` (lodash, ya presente) con `preserveState: true`.
- **Dropdown con popper**: igual que Vue pero con guard `typeof window !== 'undefined'` (requisito SSR).

## 4. SSR completo (React)
`quarkus-inertia` ya lo soporta (`SsrHandler` + `HtmlRenderer`): POST del PageObject JSON al server Node → `{head, body}` → template con `ssrBody/ssrHead`; **fallback automático a CSR** si el server SSR está caído (`recoverWithItem`).
- `vite.config.ts`: build cliente → `META-INF/resources` (igual que hoy) **+** build SSR (`build.ssr` → `dist-ssr/ssr.mjs`, input `resources/js/ssr.tsx`).
- `ssr.tsx`: `createServer` + `renderToString` + `import.meta.glob('./Pages/**/*.tsx')`.
- `app.tsx`: `hydrateRoot` si `el.dataset.serverRendered` (el template ya emite `data-server-rendered="true"`), si no `createRoot`.
- Runbook: `npm run build:ssr && node dist-ssr/ssr.mjs` (puerto 13714 vía `INERTIA_SSR_PORT`) + `mvn quarkus:dev` con `ssr-enabled=true`.
- Tests JVM: se quedan con `ssr-enabled=false` (el server SSR es un proceso Node externo); SSR se verifica con smoke manual (curl al shell HTML debe contener markup renderizado) documentado en README.

## 5. Versiones
React 19.x · react-dom 19 · `@inertiajs/react` ^3.6.1 · `@inertiajs/vite` ^3.6.1 · `@vitejs/plugin-react` · TypeScript ^5.x · Tailwind 4 (`@tailwindcss/vite`) · Vite 8 · lodash · `@popperjs/core` · uuid · Node 22 (frontend-maven-plugin, igual que pingcrm).

## 6. Orden de ejecución
1. Copiar backend + tests + resources; `mvn verify` verde en `examples/pingcrm-react`.
2. Scaffold webui (package.json, tsconfig, vite.config.ts, app.tsx, types).
3. Componentes Shared → React (inputs, botones, paginación, flash, dropdown, menú).
4. MainLayout + 12 páginas React; `npm run build` y verificación visual manual (login + CRUD completo + foto de usuario).
5. SSR: ssr.tsx + build SSR + smoke test (shell con HTML renderizado + fallback CSR).
6. README + `PingCRM-React-Tutorial.md` (diferencias React vs Vue: hook `useForm`, inputs controlados, `InertiaLayout`, `usePage`, hydrateRoot, `createServer`).
7. `.gitignore` propio + commit.

## Riesgos a resolver durante la ejecución
- **API v3 de `@inertiajs/react`**: verificar el export correcto de `createServer` (SSR) y `InertiaLayout` contra la doc v3 (el port Landish es v1/v2).
- **Browser-only APIs en SSR** (popper, `window`) → guards.
- **TypeScript estricto** contra props tipados del server (`types/index.d.ts`).

## Entregables
`examples/pingcrm-react/` completo (backend copiado + frontend React/TS + SSR + tests verdes + README + tutorial) y el plan persistido aquí.
