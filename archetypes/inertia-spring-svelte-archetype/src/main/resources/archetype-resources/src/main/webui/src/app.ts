import './styles/app.css'
import { createInertiaApp } from '@inertiajs/svelte'
import type { ResolvedComponent } from '@inertiajs/svelte'

const pages = import.meta.glob('./pages/**/*.svelte', { eager: true }) as Record<string, { default: ResolvedComponent }>

createInertiaApp({
  resolve: (name) => {
    const page = pages['./pages/' + name + '.svelte']
    if (!page) throw new Error('Page not found: ' + name)
    return page
  },
})
