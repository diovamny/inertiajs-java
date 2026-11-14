<script setup lang="ts">
import { Head, Link } from '@inertiajs/vue3'
import Layout from './Layout.vue'

defineProps<{ status: number }>()
</script>

<template>
  <Layout>
    <Head title="Error" />
    <h1 class="error-status">{{ status }}</h1>
    <p>Se ha producido un error inesperado.</p>
    <p><Link href="/">Volver al inicio</Link></p>
  </Layout>
</template>
