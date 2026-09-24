import { createSSRApp, h } from 'vue'
import { renderToString } from 'vue/server-renderer'
import { createInertiaApp } from '@inertiajs/vue3'

// Server-side entry for `npm run build:ssr`: renders any page to
// { head, body } for the Node sidecar (ssr-server.mjs). Mirrors app.ts
// so SSR output hydrates cleanly on the client.
export async function render(page: unknown) {
  return createInertiaApp({
    page,
    render: renderToString,
    resolve: (name) => {
      const pages = import.meta.glob('./pages/**/*.vue', { eager: true }) as Record<
        string,
        { default: unknown }
      >
      const resolved = pages['./pages/' + name + '.vue']
      if (!resolved) throw new Error('Page not found: ' + name)
      return resolved
    },
    setup({ App, props, plugin }) {
      return createSSRApp({ render: () => h(App, props) }).use(plugin)
    },
  })
}
