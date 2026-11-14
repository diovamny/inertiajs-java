# Client Setup

The client is a normal Vite application using the official Inertia adapter for
your framework. The Java backend does not bundle or transform the frontend; it
only needs the built assets and the Vite manifest.

## Install

::: code-group

```bash [Vue 3]
npm install @inertiajs/vue3@3.11.0 @inertiajs/core@3.11.0 @inertiajs/vite@3.11.0
npm install -D vite @vitejs/plugin-vue typescript@6 vue-tsc
```

```bash [React 19]
npm install @inertiajs/react@3.11.0 @inertiajs/core@3.11.0 @inertiajs/vite@3.11.0
npm install -D vite @vitejs/plugin-react typescript@6
```

```bash [Svelte 5]
npm install @inertiajs/svelte@3.11.0 @inertiajs/core@3.11.0 @inertiajs/vite@3.11.0
npm install -D vite @sveltejs/vite-plugin-svelte typescript@6 svelte-check
```

:::

The starters pin the official client at exactly `3.11.0` with
`save-exact=true`. Live Props metadata requires a `3.11.0` client; older
clients ignore the extra page fields.

## Create the application

::: code-group

```ts [Vue 3]
import './styles/app.css'
import { createInertiaApp } from '@inertiajs/vue3'
import { createApp, h } from 'vue'
import type { DefineComponent } from 'vue'

createInertiaApp({
  resolve: (name) => {
    const pages = import.meta.glob<{ default: DefineComponent }>(
      './pages/**/*.vue',
      { eager: true },
    )
    const page = pages[`./pages/${name}.vue`]
    if (!page) throw new Error(`Page not found: ${name}`)
    return page
  },
  setup({ el, App, props, plugin }) {
    createApp({ render: () => h(App, props) })
      .use(plugin)
      .mount(el)
  },
})
```

```tsx [React 19]
import './styles/app.css'
import { createRoot, hydrateRoot } from 'react-dom/client'
import { createInertiaApp } from '@inertiajs/react'
import type { ResolvedComponent } from '@inertiajs/react'

const pages = import.meta.glob('./pages/**/*.tsx', { eager: true }) as Record<
  string,
  { default: ResolvedComponent }
>

createInertiaApp({
  resolve: (name) => {
    const page = pages[`./pages/${name}.tsx`]
    if (!page) throw new Error(`Page not found: ${name}`)
    return page
  },
  setup({ el, App, props }) {
    if (el.dataset.serverRendered === 'true') {
      hydrateRoot(el, <App {...props} />)
    } else {
      createRoot(el).render(<App {...props} />)
    }
  },
})
```

```ts [Svelte 5]
import './styles/app.css'
import { createInertiaApp } from '@inertiajs/svelte'
import type { ResolvedComponent } from '@inertiajs/svelte'

const pages = import.meta.glob<ResolvedComponent>(
  './pages/**/*.svelte',
  { eager: true },
)

createInertiaApp({
  resolve: (name) => {
    const page = pages[`./pages/${name}.svelte`]
    if (!page) throw new Error(`Page not found: ${name}`)
    return page
  },
})
```

:::

The resolver maps the component name coming from the server to a file:

| Server component | File |
|---|---|
| `Welcome` | `src/pages/Welcome.vue` (or `.tsx`, `.svelte`) |
| `Contacts/Index` | `src/pages/Contacts/Index.vue` |
| `Contacts/Edit` | `src/pages/Contacts/Edit.vue` |

Component names are case-sensitive. The starter throws a clear error when a
page is missing instead of rendering a blank screen.

## Vite configuration

Use the official `inertia()` plugin and emit the manifest at
`.vite/manifest.json`:

::: code-group

```ts [Vue 3]
import path from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import inertia from '@inertiajs/vite'

const root = import.meta.dirname

export default defineConfig(({ isSsrBuild }) => ({
  plugins: [vue(), inertia()],
  resolve: { alias: { '@': path.resolve(root, 'src') } },
  build: {
    outDir: path.resolve(root, '../../main/resources/static'),
    emptyOutDir: false,
    manifest: '.vite/manifest.json',
    rollupOptions: {
      input: path.resolve(root, 'src/app.ts'),
      output: {
        entryFileNames: isSsrBuild ? '[name].js' : 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name][extname]',
      },
    },
  },
  server: { port: 5173 },
}))
```

```ts [React 19]
import path from 'node:path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import inertia from '@inertiajs/vite'

const root = import.meta.dirname

export default defineConfig({
  plugins: [react(), inertia()],
  build: {
    outDir: path.resolve(root, '../../main/resources/static'),
    manifest: '.vite/manifest.json',
    rollupOptions: { input: path.resolve(root, 'src/app.tsx') },
  },
  server: { port: 5173 },
})
```

```ts [Svelte 5]
import path from 'node:path'
import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import inertia from '@inertiajs/vite'

const root = import.meta.dirname

export default defineConfig({
  plugins: [svelte(), inertia()],
  build: {
    outDir: path.resolve(root, '../../main/resources/static'),
    manifest: '.vite/manifest.json',
    rollupOptions: { input: path.resolve(root, 'src/app.ts') },
  },
  server: { port: 5173 },
})
```

:::

The Java adapter reads the manifest to emit hashed asset URLs and to compute
the asset version. Keeping `manifest: true` (or the explicit
`.vite/manifest.json` path) is required for `inertia.version-strategy=vite-manifest`.

## Scripts

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "build:ssr": "vite build --ssr src/ssr.ts --outDir dist-ssr",
    "ssr": "node dist-ssr/ssr.js",
    "typecheck": "vue-tsc --noEmit"
  }
}
```

For React use `src/ssr.tsx`; for Svelte `src/ssr.ts`. The SSR entry uses the
official `createServer` from `@inertiajs/*/server` and is covered in
[SSR](/guide/ssr).

## Development workflow

1. Run `npm run dev` in `src/main/webui` (Vite on `:5173`).
2. Run the backend (`mvn spring-boot:run` or `mvn quarkus:dev`).
3. Open the backend URL. During development, either proxy the Vite dev server
   from the backend or build the assets once and let the manifest serve them.
4. Before packaging, run `npm run build` so the Java process has fresh assets.

## TypeScript

The starters use TypeScript 6 with `strict` mode and framework-specific
checkers (`vue-tsc`, `tsc --noEmit`, `svelte-check`). Page props should be
typed explicitly; the server is the source of truth, but TypeScript catches
mismatches early:

```ts
interface Contact {
  id: number
  name: string
  email: string
  phone: string | null
}

defineProps<{ contacts: Contact[] }>()
```

Next: [Root Template](/guide/root-template) · [Forms](/guide/forms) ·
[SSR](/guide/ssr)
