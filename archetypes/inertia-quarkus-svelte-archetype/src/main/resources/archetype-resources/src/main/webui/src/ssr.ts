import createServer from '@inertiajs/svelte/server'
import { createInertiaApp } from '@inertiajs/svelte'
import { render } from 'svelte/server'
import type { ResolvedComponent } from '@inertiajs/svelte'

createServer((page) =>
  createInertiaApp({
    page,
    resolve: (name) => {
      const pages = import.meta.glob<ResolvedComponent>(
        './pages/**/*.svelte',
        { eager: true }
      )
      const resolved = pages[`./pages/${name}.svelte`]
      if (!resolved) throw new Error(`Page not found: ${name}`)
      return resolved
    },
    setup: ({ App, props }) => render(App, { props }),
  })
)
