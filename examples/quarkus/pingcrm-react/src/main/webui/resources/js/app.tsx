import type { ResolvedComponent } from '@inertiajs/react'
import '../css/app.css'
import { createRoot, hydrateRoot } from 'react-dom/client'
import { createInertiaApp } from '@inertiajs/react'

const pages = import.meta.glob('./Pages/**/*.tsx', { eager: true })

createInertiaApp({
  resolve: (name) => pages[`./Pages/${name}.tsx`] as { default: ResolvedComponent },
  title: (title) => (title ? `${title} - Ping CRM` : 'Ping CRM'),
  setup({ el, App, props }) {
    if (el.dataset.serverRendered) {
      hydrateRoot(el, <App {...props} />)
    } else {
      createRoot(el).render(<App {...props} />)
    }
  },
})
