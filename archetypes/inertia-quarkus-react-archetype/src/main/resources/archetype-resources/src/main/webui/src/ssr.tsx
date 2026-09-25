import { renderToString } from 'react-dom/server'
import { createInertiaApp } from '@inertiajs/react'
import type { ResolvedComponent } from '@inertiajs/react'

// Server-side entry for `npm run build:ssr`: renders any page to
// { head, body } for the Node sidecar (ssr-server.mjs). Mirrors app.tsx
// so SSR output hydrates cleanly on the client.
export async function render(page: unknown) {
  return createInertiaApp({
    page,
    render: renderToString,
    resolve: (name) => {
      const pages = import.meta.glob('./pages/**/*.tsx', { eager: true }) as Record<
        string,
        { default: ResolvedComponent }
      >
      const resolved = pages['./pages/' + name + '.tsx']
      if (!resolved) throw new Error('Page not found: ' + name)
      return resolved
    },
    setup: ({ App, props }) => <App {...props} />,
  })
}
