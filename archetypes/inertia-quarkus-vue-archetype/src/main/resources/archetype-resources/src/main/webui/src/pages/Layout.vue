<script setup lang="ts">
import { computed } from 'vue'
import { Link, usePage } from '@inertiajs/vue3'

type FlashProps = { success?: string; error?: string }

const page = usePage<FlashProps>()
const flashSuccess = computed(() => page.props.success)
const flashError = computed(() => page.props.error)
</script>

<template>
  <div class="layout">
    <nav>
      <Link href="/">Inicio</Link>
      <Link href="/contacts">Contactos</Link>
    </nav>
    <div v-if="flashSuccess" class="alert alert-success" role="status">{{ flashSuccess }}</div>
    <div v-if="flashError" class="alert alert-error" role="alert">{{ flashError }}</div>
    <main>
      <slot />
    </main>
  </div>
</template>
