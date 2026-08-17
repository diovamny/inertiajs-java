<template>
  <div class="p-4">
    <div class="flex justify-content-between align-items-center mb-4">
      <h1 class="text-2xl font-bold">{{ editing ? 'Editar Persona' : 'Nueva Persona' }}</h1>
      <InertiaLink href="/persons" class="p-button p-component p-button-text p-button-sm">
        <i class="pi pi-arrow-left mr-2"></i>Volver
      </InertiaLink>
    </div>

    <div class="card p-4 surface-card border-round shadow-1">
      <form @submit.prevent="submit">
        <div class="field mb-3">
          <label for="name" class="font-medium mb-1 block">Nombre</label>
          <InputText id="name" v-model="form.name" class="w-full" :class="{ 'p-invalid': errors.name }" />
          <small v-if="errors.name" class="p-error">{{ errors.name }}</small>
        </div>

        <div class="field mb-3">
          <label for="lastName" class="font-medium mb-1 block">Apellido</label>
          <InputText id="lastName" v-model="form.lastName" class="w-full" :class="{ 'p-invalid': errors.lastName }" />
          <small v-if="errors.lastName" class="p-error">{{ errors.lastName }}</small>
        </div>

        <div class="field mb-3">
          <label for="email" class="font-medium mb-1 block">Email</label>
          <InputText id="email" v-model="form.email" type="email" class="w-full"
            :class="{ 'p-invalid': errors.email }" />
          <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
        </div>

        <div class="field mb-3">
          <label for="phone" class="font-medium mb-1 block">Teléfono</label>
          <InputText id="phone" v-model="form.phone" class="w-full" :class="{ 'p-invalid': errors.phone }" />
          <small v-if="errors.phone" class="p-error">{{ errors.phone }}</small>
        </div>

        <div class="field mb-4">
          <label class="font-medium mb-1 block">Estado</label>
          <InputSwitch v-model="form.active" />
          <span class="ml-2">{{ form.active ? 'Activo' : 'Inactivo' }}</span>
        </div>

        <div class="flex gap-2">
          <Button type="submit" label="Guardar" icon="pi pi-check" />
          <InertiaLink href="/persons" class="p-button p-component p-button-secondary">
            Cancelar
          </InertiaLink>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { router, Link as InertiaLink } from '@inertiajs/vue3'

const props = defineProps({
  person: Object,
  editing: Boolean,
  errors: Object
})

const form = reactive({
  name: props.person?.name || '',
  lastName: props.person?.lastName || '',
  email: props.person?.email || '',
  phone: props.person?.phone || '',
  active: props.person?.active !== undefined ? props.person.active : true
})

const errors = reactive(props.errors || {})

function submit() {
  const url = props.editing ? `/persons/${props.person.id}` : '/persons'
  router.post(url, { ...form }, {
    errorBag: 'personForm',
    onError: (errs) => {
      Object.assign(errors, errs)
    }
  })
}
</script>
