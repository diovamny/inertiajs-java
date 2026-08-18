<template>
  <div class="flash-container">
    <Toast position="top-right" group="flash" />
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { usePage } from '@inertiajs/vue3'
import { useToast } from 'primevue/usetoast'

const toast = useToast()
const page = usePage()

watch(() => page.props, (props) => {
  if (!props) return

  if (props.success) {
    toast.add({
      severity: 'success',
      summary: 'Éxito',
      detail: props.success,
      life: 4000,
      group: 'flash'
    })
  }

  if (props.error) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: props.error,
      life: 6000,
      group: 'flash'
    })
  }

  if (props.warning) {
    toast.add({
      severity: 'warn',
      summary: 'Advertencia',
      detail: props.warning,
      life: 5000,
      group: 'flash'
    })
  }

  if (props.info) {
    toast.add({
      severity: 'info',
      summary: 'Información',
      detail: props.info,
      life: 4000,
      group: 'flash'
    })
  }
}, { immediate: true, deep: true })
</script>