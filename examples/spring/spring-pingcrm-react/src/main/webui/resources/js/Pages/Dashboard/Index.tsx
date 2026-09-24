import { Head } from '@inertiajs/react'
import Layout from '@/Shared/Layout'

export default function Dashboard() {
  return (
    <Layout>
      <Head title="Dashboard" />
      <h1 className="mb-8 text-3xl font-bold">Dashboard</h1>
      <p className="mb-8 leading-normal">
        Hey there! Welcome to Ping CRM, a demo app designed to help illustrate how{' '}
        <a className="text-indigo-500 hover:text-orange-600 underline" href="https://inertiajs.com">
          Inertia.js
        </a>{' '}
        works.
      </p>
    </Layout>
  )
}
