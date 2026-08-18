import { renderToString } from 'react-dom/server'
import { App } from '@inertiajs/react'
import createServer from '@inertiajs/react/server'
import type { ResolvedComponent } from '@inertiajs/react'
import type { Page } from '@inertiajs/core'
import type { PageProps } from '@/types'

const pages = import.meta.glob('./Pages/**/*.tsx', { eager: true })

export default function render(page: Page<PageProps>) {
  const initialComponent = (pages[`./Pages/${page.component}.tsx`] as { default: ResolvedComponent }).default
  const head: string[] = []

  const app = renderToString(
    <App
      initialPage={page}
      initialComponent={initialComponent}
      resolveComponent={(name) => (pages[`./Pages/${name}.tsx`] as { default: ResolvedComponent }).default}
      titleCallback={(title) => (title ? `${title} - Ping CRM` : 'Ping CRM')}
      onHeadUpdate={(elements) => {
        head.length = 0
        head.push(...elements)
      }}
    />,
  )

  return { head, body: app }
}

;(() => { createServer(render) })()