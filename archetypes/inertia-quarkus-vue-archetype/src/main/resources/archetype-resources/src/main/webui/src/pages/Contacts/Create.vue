<script setup lang="ts">
import { Head, Link, useForm } from '@inertiajs/vue3'
import Layout from '../Layout.vue'

const form = useForm({ name: '', email: '', phone: '' })

function submit() {
  form.post('/contacts')
}
</script>

<template>
  <Layout>
    <Head title="Nuevo contacto" />
    <h1>Nuevo contacto</h1>
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
        <button class="button" type="submit" :disabled="form.processing">Guardar</button>
        <Link href="/contacts">Cancelar</Link>
      </div>
    </form>
  </Layout>
</template>
