import { useEffect } from 'react'
import { usePage } from '@inertiajs/react'
import type { PageProps } from '@/types'

/**
 * The server reports validation failures as flash errors that land in
 * top-level `props.errors` after a 303 redirect. This keeps them in sync
 * with a `useForm` instance so fields display their `form.errors`.
 */
export function useSyncErrors(form: { setError: (key: any, value: any) => void }) {
  const { errors } = usePage<PageProps>().props
  useEffect(() => {
    if (!errors) return
    for (const [key, value] of Object.entries(errors)) {
      if (typeof value === 'string' && value !== '') {
        form.setError(key, value)
      }
    }
  }, [errors])
}
