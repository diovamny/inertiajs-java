<script setup lang="ts">
import { Head, Link, router } from '@inertiajs/vue3'
import Layout from '../Layout.vue'

type Contact = { id: number; name: string; email: string; phone: string | null }

defineProps<{ contacts: Contact[] }>()

function destroy(contact: Contact) {
  if (!confirm(`¿Eliminar el contacto ${contact.name}?`)) return
  router.delete(`/contacts/${contact.id}`, { preserveScroll: true })
}
</script>

<template>
  <Layout>
    <Head title="Contactos" />
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
        <tr v-for="contact in contacts" :key="contact.id">
          <td>
            <Link :href="`/contacts/${contact.id}/edit`">{{ contact.name }}</Link>
          </td>
          <td>{{ contact.email }}</td>
          <td>{{ contact.phone ?? '' }}</td>
          <td>
            <div class="actions">
              <Link :href="`/contacts/${contact.id}/edit`">Editar</Link>
              <button type="button" class="button danger" @click="destroy(contact)">Eliminar</button>
            </div>
          </td>
        </tr>
        <tr v-if="contacts.length === 0">
          <td colspan="4">No hay contactos todavía.</td>
        </tr>
      </tbody>
    </table>
  </Layout>
</template>
