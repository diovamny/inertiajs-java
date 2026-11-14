import { Head, Link } from '@inertiajs/react'
import Layout from './Layout'

export default function ErrorPage({ status }: { status: number }) {
  return (
    <Layout>
      <Head title="Error" />
      <h1 className="error-status">{status}</h1>
      <p>Se ha producido un error inesperado.</p>
      <p>
        <Link href="/">Volver al inicio</Link>
      </p>
    </Layout>
  )
}
