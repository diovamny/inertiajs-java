import { Head, router, useForm, usePage } from '@inertiajs/react'
import type { FormEvent } from 'react'
import type { PageProps } from '@/types'

interface ProbeEntry {
  id: number
  name: string
}

interface ProbePageProps extends PageProps {
  greeting: string
  notice?: string | null
  slow?: string | null
  entries: ProbeEntry[]
}

/**
 * E2E feature probe (Fase E): exercises the full Inertia v3 contract through
 * the official React client on a public route (no login required).
 *
 * The probe is served under a variant base (/e2e-probe on Spring/JAX-RS,
 * /e2e-probe-rx on Reactive Routes): probeBase() derives it from the current
 * URL so every form post and visit targets the same transport
 * (E2E_PROBE_BASE). It is only invoked from event handlers, so server-side
 * rendering never touches window.
 */
function probeBase(): string {
  return window.location.pathname.replace(/\/target\/?$/, '').replace(/\/+$/, '')
}

export default function ProbeIndex() {
  const { props } = usePage<ProbePageProps>()
  const form = useForm({ name: '', email: '' })
  const secondary = useForm({ title: '', body: '' })
  const upload = useForm<{ photo: File | null }>({ photo: null })

  const bagErrors = ((props.errors ?? {}) as Record<string, unknown>)
    .probeSecondary as Record<string, string> | undefined

  const submitPrimary = (e: FormEvent) => {
    e.preventDefault()
    form.post(probeBase() + '/validate')
  }

  const submitSecondary = (e: FormEvent) => {
    e.preventDefault()
    secondary.post(probeBase() + '/validate-secondary', {
      headers: { 'X-Inertia-Error-Bag': 'probeSecondary' },
    })
  }

  const submitUpload = (e: FormEvent) => {
    e.preventDefault()
    upload.post(probeBase() + '/upload')
  }

  return (
    <div className="mx-auto max-w-3xl p-8">
      <Head title="E2E Probe" />
      <h1 className="mb-2 text-3xl font-bold">E2E Probe</h1>
      <p data-testid="probe-greeting" className="mb-6 text-gray-600">
        {props.greeting}
      </p>

      {props.success && (
        <p data-testid="probe-flash" className="mb-6 rounded bg-green-500 p-3 text-white">
          {props.success}
        </p>
      )}

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Validation</h2>
        <form onSubmit={submitPrimary}>
          <label className="mb-1 block text-sm font-medium" htmlFor="probe-name">
            Name
          </label>
          <input
            id="probe-name"
            type="text"
            className="mb-1 w-full rounded border p-2"
            value={form.data.name}
            onChange={(e) => form.setData('name', e.target.value)}
          />
          {form.errors.name && <p className="mb-2 text-sm text-red-500">{form.errors.name}</p>}
          <label className="mb-1 block text-sm font-medium" htmlFor="probe-email">
            Email
          </label>
          <input
            id="probe-email"
            type="text"
            className="mb-1 w-full rounded border p-2"
            value={form.data.email}
            onChange={(e) => form.setData('email', e.target.value)}
          />
          {form.errors.email && <p className="mb-2 text-sm text-red-500">{form.errors.email}</p>}
          <button type="submit" className="btn-indigo mt-2">
            Submit probe form
          </button>
        </form>
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Named error bag</h2>
        <form onSubmit={submitSecondary}>
          <label className="mb-1 block text-sm font-medium" htmlFor="probe-title">
            Title
          </label>
          <input
            id="probe-title"
            type="text"
            className="mb-1 w-full rounded border p-2"
            value={secondary.data.title}
            onChange={(e) => secondary.setData('title', e.target.value)}
          />
          <label className="mb-1 block text-sm font-medium" htmlFor="probe-body">
            Body
          </label>
          <input
            id="probe-body"
            type="text"
            className="mb-1 w-full rounded border p-2"
            value={secondary.data.body}
            onChange={(e) => secondary.setData('body', e.target.value)}
          />
          <button type="submit" className="btn-indigo mt-2">
            Submit probe secondary
          </button>
        </form>
        {bagErrors && (
          <div data-testid="probe-bag-errors" className="mt-3">
            <h3 className="font-medium">probeSecondary.errors</h3>
            {Object.entries(bagErrors).map(([key, message]) => (
              <p key={key} className="text-sm text-red-500">
                {message}
              </p>
            ))}
          </div>
        )}
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Deferred prop</h2>
        <p data-testid="probe-slow" className="mb-2">
          {props.slow ?? 'pending'}
        </p>
        <button
          type="button"
          className="btn-indigo"
          onClick={() => router.reload({ only: ['slow'] })}
        >
          Load slow prop
        </button>
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Once prop</h2>
        {props.notice && (
          <p data-testid="probe-notice" className="mb-2">
            {props.notice}
          </p>
        )}
        <button
          type="button"
          className="btn-indigo"
          onClick={() =>
            router.reload({ headers: { 'X-Inertia-Except-Once-Props': 'notice' } })
          }
        >
          Suppress notice
        </button>
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Mergeable entries</h2>
        <p data-testid="probe-entries-count" className="mb-2">
          {props.entries.length} entries
        </p>
        <ul data-testid="probe-entries" className="mb-2">
          {props.entries.map((entry) => (
            <li key={entry.id}>
              {entry.id}: {entry.name}
            </li>
          ))}
        </ul>
        <button
          type="button"
          className="btn-indigo"
          onClick={() => router.reload({ only: ['entries'] })}
        >
          Fetch next entry
        </button>
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Instant visit</h2>
        <button
          type="button"
          className="btn-indigo"
          onClick={() =>
            router.visit(probeBase() + '/target?delay=2', {
              component: 'Probe/Target',
              pageProps: (current: Record<string, unknown>, shared: Record<string, unknown>) => ({
                ...shared,
                greeting: `Navigating from probe (was: "${String(current.greeting)}")`,
              }),
            })
          }
        >
          Visit target instantly
        </button>
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">Redirect back</h2>
        <button
          type="button"
          className="btn-indigo"
          onClick={() => router.post(probeBase() + '/redirect-back')}
        >
          Submit and redirect back
        </button>
      </section>

      <section className="mb-8 rounded bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-bold">File upload</h2>
        <form onSubmit={submitUpload}>
          <input
            id="probe-photo"
            type="file"
            className="mb-2 block"
            onChange={(e) => upload.setData('photo', e.target.files?.[0] ?? null)}
          />
          {upload.errors.photo && <p className="mb-2 text-sm text-red-500">{upload.errors.photo}</p>}
          <button type="submit" className="btn-indigo">
            Upload photo
          </button>
        </form>
      </section>
    </div>
  )
}
