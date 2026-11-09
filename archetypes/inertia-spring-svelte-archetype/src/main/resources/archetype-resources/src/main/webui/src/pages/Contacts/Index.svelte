<script lang="ts">
  import { Link, router } from '@inertiajs/svelte'
  import Layout from '../Layout.svelte'

  type Contact = { id: number; name: string; email: string; phone: string | null }

  let { contacts }: { contacts: Contact[] } = $props()

  function destroy(contact: Contact) {
    if (!confirm(`¿Eliminar el contacto ${contact.name}?`)) return
    router.delete(`/contacts/${contact.id}`, { preserveScroll: true })
  }
</script>

<svelte:head>
  <title>Contactos</title>
</svelte:head>

<Layout>
  <div class="page-header">
    <h1>Contactos</h1>
    <Link class="button" href="/contacts/create">Nuevo contacto</Link>
  </div>
  <table class="table">
    <thead>
      <tr>
        <th>Nombre</th>
        <th>Email</th>
        <th>Teléfono</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      {#each contacts as contact (contact.id)}
        <tr>
          <td><Link href={`/contacts/${contact.id}/edit`}>{contact.name}</Link></td>
          <td>{contact.email}</td>
          <td>{contact.phone ?? ''}</td>
          <td>
            <div class="actions">
              <Link href={`/contacts/${contact.id}/edit`}>Editar</Link>
              <button type="button" class="button danger" onclick={() => destroy(contact)}>Eliminar</button>
            </div>
          </td>
        </tr>
      {/each}
      {#if contacts.length === 0}
        <tr>
          <td colspan="4">No hay contactos todavía.</td>
        </tr>
      {/if}
    </tbody>
  </table>
</Layout>
