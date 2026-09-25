import { Head, Link, usePage } from '@inertiajs/react'
import type { PageProps } from '@/types'

interface TargetProps extends PageProps {
  greeting: string
}

/** Instant-visit target for the E2E probe (Fase E). */
export default function ProbeTarget() {
  const { props } = usePage<TargetProps>()
  return (
    <div className="mx-auto max-w-3xl p-8">
      <Head title="Probe Target" />
      <h1 className="mb-2 text-3xl font-bold">Probe Target</h1>
      <p data-testid="probe-target-greeting" className="mb-6">
        {props.greeting}
      </p>
      <Link href=".." className="text-indigo-500 hover:underline">
        Back to probe
      </Link>
    </div>
  )
}
