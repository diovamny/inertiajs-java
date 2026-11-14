<script lang="ts">
  import type { Snippet } from 'svelte'
  import { Link, usePage } from '@inertiajs/svelte'

  let { children }: { children?: Snippet } = $props()

  let page = usePage()
  let flashSuccess = $derived(page.props.success as string | undefined)
  let flashError = $derived(page.props.error as string | undefined)
</script>

<div class="layout">
  <nav>
    <Link href="/">Inicio</Link>
    <Link href="/contacts">Contactos</Link>
  </nav>
  {#if flashSuccess}
    <div class="alert alert-success" role="status">{flashSuccess}</div>
  {/if}
  {#if flashError}
    <div class="alert alert-error" role="alert">{flashError}</div>
  {/if}
  <main>
    {@render children?.()}
  </main>
</div>
