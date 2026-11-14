<script lang="ts">
  import { Link, useForm } from '@inertiajs/svelte'
  import Layout from '../Layout.svelte'

  let form = useForm({ name: '', email: '', phone: '' })

  function submit(event: SubmitEvent) {
    event.preventDefault()
    form.post('/contacts')
  }
</script>

<svelte:head>
  <title>Nuevo contacto</title>
</svelte:head>

<Layout>
  <h1>Nuevo contacto</h1>
  <form class="form" onsubmit={submit}>
    <label>
      <span>Nombre</span>
      <input type="text" autocomplete="name" bind:value={form.name} />
      {#if form.errors.name}
        <span class="error">{form.errors.name}</span>
      {/if}
    </label>
    <label>
      <span>Email</span>
      <input type="email" autocomplete="email" bind:value={form.email} />
      {#if form.errors.email}
        <span class="error">{form.errors.email}</span>
      {/if}
    </label>
    <label>
      <span>Teléfono</span>
      <input type="text" autocomplete="tel" bind:value={form.phone} />
      {#if form.errors.phone}
        <span class="error">{form.errors.phone}</span>
      {/if}
    </label>
    <div class="form-actions">
      <button class="button" type="submit" disabled={form.processing}>Guardar</button>
      <Link href="/contacts">Cancelar</Link>
    </div>
  </form>
</Layout>
