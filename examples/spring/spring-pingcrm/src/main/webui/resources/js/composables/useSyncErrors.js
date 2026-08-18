import { watch } from 'vue'
import { usePage } from '@inertiajs/vue3'

/**
 * The server reports validation failures as flash errors that land in
 * top-level `props.errors` after a 303 redirect. This keeps them in sync
 * with a `useForm` instance so fields display their `form.errors`.
 */
export function useSyncErrors(form) {
  const page = usePage()
  watch(
    () => page.props.errors,
    (errors) => {
      if (!errors) return
      for (const [key, value] of Object.entries(errors)) {
        if (typeof value === 'string' && value !== '') {
          form.setError(key, value)
        }
      }
    },
    { immediate: true },
  )
}
