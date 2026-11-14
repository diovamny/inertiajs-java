<script lang="ts">
  import { untrack } from 'svelte'
  import { Link, useForm } from '@inertiajs/svelte'
  import Layout from '../Layout.svelte'

  type Contact = { id: number; name: string; email: string; phone: string | null }

  let { contact }: { contact: Contact } = $props()

  let form = useForm(
    untrack(() => ({
      name: contact.name,
      email: contact.email,
      phone: contact.phone ?? '',
    }))
  )

  function submit(event: SubmitEvent) {
    event.preventDefault()
    form.put(`/contacts/${contact.id}`, { preserveScroll: true })
  }
</script>

<svelte:head>
  <title>Editar {contact.name}</title>
</svelte:head>

<Layout>
  <h1>Editar contacto</h1>
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
      <button class="button" type="submit" disabled={form.processing}>Guardar cambios</button>
      <Link href="/contacts">Cancelar</Link>
    </div>
  </form>
</Layout>
