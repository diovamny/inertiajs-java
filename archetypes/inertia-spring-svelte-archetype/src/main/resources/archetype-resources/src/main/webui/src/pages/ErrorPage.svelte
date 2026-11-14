<script lang="ts">
  import { Link } from '@inertiajs/svelte'
  import Layout from './Layout.svelte'

  let { status }: { status: number } = $props()
</script>

<svelte:head>
  <title>Error</title>
</svelte:head>

<Layout>
  <h1 class="error-status">{status}</h1>
  <p>Se ha producido un error inesperado.</p>
  <p><Link href="/">Volver al inicio</Link></p>
</Layout>
