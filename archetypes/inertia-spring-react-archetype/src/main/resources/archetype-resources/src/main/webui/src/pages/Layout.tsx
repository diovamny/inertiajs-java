import type { ReactNode } from 'react'
import { Link, usePage } from '@inertiajs/react'

type FlashProps = { success?: string; error?: string }

export default function Layout({ children }: { children: ReactNode }) {
  const { props } = usePage<FlashProps>()

  return (
    <div className="layout">
      <nav>
        <Link href="/">Inicio</Link>
        <Link href="/contacts">Contactos</Link>
      </nav>
      {props.success && (
        <div className="alert alert-success" role="status">
          {props.success}
        </div>
      )}
      {props.error && (
        <div className="alert alert-error" role="alert">
          {props.error}
        </div>
      )}
      <main>{children}</main>
    </div>
  )
}
