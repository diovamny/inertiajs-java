import createServer from '@inertiajs/react/server'
import { createInertiaApp } from '@inertiajs/react'
import ReactDOMServer from 'react-dom/server'
import type { ResolvedComponent } from '@inertiajs/react'

createServer((page) =>
  createInertiaApp({
    page,
    render: ReactDOMServer.renderToString,
    resolve: (name) => {
      const pages = import.meta.glob<{ default: ResolvedComponent }>(
        './pages/**/*.tsx',
        { eager: true }
      )
      const resolved = pages[`./pages/${name}.tsx`]
      if (!resolved) throw new Error(`Page not found: ${name}`)
      return resolved
    },
    setup: ({ App, props }) => <App {...props} />,
  })
)
