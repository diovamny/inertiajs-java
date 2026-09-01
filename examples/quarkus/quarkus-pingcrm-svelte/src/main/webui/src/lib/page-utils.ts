import { page } from '@inertiajs/svelte'
import { get } from 'svelte/store'

export function getFlash() {
  const p = get(page)
  return p?.props?.flash || {}
}

export function getPage() {
  return get(page)
}
