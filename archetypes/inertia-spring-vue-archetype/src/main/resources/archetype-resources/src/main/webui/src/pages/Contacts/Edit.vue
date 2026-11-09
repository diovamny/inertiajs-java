<script setup lang="ts">
import { Head, Link, useForm } from '@inertiajs/vue3'
import Layout from '../Layout.vue'

type Contact = { id: number; name: string; email: string; phone: string | null }

const props = defineProps<{ contact: Contact }>()

const form = useForm({
  name: props.contact.name,
  email: props.contact.email,
  phone: props.contact.phone ?? '',
})

function submit() {
  form.put(`/contacts/${props.contact.id}`, { preserveScroll: true })
}
</script>

<template>
  <Layout>
    <Head :title="`Editar ${contact.name}`" />
    <h1>Editar contacto</h1>
    <form class="form" @submit.prevent="submit">
      <label>
        <span>Nombre</span>
        <input v-model="form.name" type="text" autocomplete="name" />
        <span v-if="form.errors.name" class="error">{{ form.errors.name }}</span>
      </label>
      <label>
        <span>Email</span>
        <input v-model="form.email" type="email" autocomplete="email" />
        <span v-if="form.errors.email" class="error">{{ form.errors.email }}</span>
      </label>
      <label>
        <span>Teléfono</span>
        <input v-model="form.phone" type="text" autocomplete="tel" />
        <span v-if="form.errors.phone" class="error">{{ form.errors.phone }}</span>
      </label>
      <div class="form-actions">
        <button class="button" type="submit" :disabled="form.processing">Guardar cambios</button>
        <Link href="/contacts">Cancelar</Link>
      </div>
    </form>
  </Layout>
</template>
