import { createInertiaApp } from '@inertiajs/svelte'
import { render as renderSvelte } from 'svelte/server'
import type { ResolvedComponent } from '@inertiajs/svelte'

// Server-side entry for `npm run build:ssr`: renders any page to
// { head, body } for the Node sidecar (ssr-server.mjs). Mirrors app.ts
// so SSR output hydrates cleanly on the client.
export async function render(page: unknown) {
  return createInertiaApp({
    page,
    resolve: (name) => {
      const pages = import.meta.glob('./pages/**/*.svelte', { eager: true }) as Record<
        string,
        { default: ResolvedComponent }
      >
      const resolved = pages['./pages/' + name + '.svelte']
      if (!resolved) throw new Error('Page not found: ' + name)
      return resolved
    },
    setup({ App, props }) {
      return renderSvelte(App, { props })
    },
  })
}
