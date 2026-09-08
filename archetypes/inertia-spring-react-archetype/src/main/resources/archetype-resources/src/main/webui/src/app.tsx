import './styles/app.css'
import { createRoot } from 'react-dom/client'
import { createInertiaApp } from '@inertiajs/react'
import type { ResolvedComponent } from '@inertiajs/react'

const pages = import.meta.glob('./pages/**/*.tsx', { eager: true }) as Record<string, { default: ResolvedComponent }>

createInertiaApp({
  resolve: (name) => {
    const page = pages['./pages/' + name + '.tsx']
    if (!page) throw new Error('Page not found: ' + name)
    return page
  },
  setup({ el, App, props }) {
    createRoot(el).render(<App {...props} />)
  },
})
