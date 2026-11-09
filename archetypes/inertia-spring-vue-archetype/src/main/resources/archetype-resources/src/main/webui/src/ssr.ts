import createServer from '@inertiajs/vue3/server'
import { createInertiaApp } from '@inertiajs/vue3'
import { createSSRApp, h } from 'vue'
import { renderToString } from 'vue/server-renderer'
import type { DefineComponent } from 'vue'

createServer((page) =>
  createInertiaApp({
    page,
    render: renderToString,
    resolve: (name) => {
      const pages = import.meta.glob<{ default: DefineComponent }>(
        './pages/**/*.vue',
        { eager: true }
      )
      const resolved = pages[`./pages/${name}.vue`]
      if (!resolved) throw new Error(`Page not found: ${name}`)
      return resolved
    },
    setup({ App, props, plugin }) {
      return createSSRApp({ render: () => h(App, props) }).use(plugin)
    },
  })
)
