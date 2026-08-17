<template>
  <div class="p-4">
    <div class="flex justify-content-between align-items-center mb-4">
      <h1 class="text-2xl font-bold">{{ editing ? 'Editar Empleado' : 'Nuevo Empleado' }}</h1>
      <InertiaLink href="/employees" class="p-button p-component p-button-text p-button-sm">
        <i class="pi pi-arrow-left mr-2"></i>Volver
      </InertiaLink>
    </div>

    <div class="card p-4 surface-card border-round shadow-1">
      <form @submit.prevent="submit">
        <div class="grid">
          <div class="col-6">
            <div class="field mb-3">
              <label for="firstName" class="font-medium mb-1 block">Nombre</label>
              <InputText id="firstName" v-model="form.firstName" class="w-full"
                :class="{ 'p-invalid': errors.firstName }" />
              <small v-if="errors.firstName" class="p-error">{{ errors.firstName }}</small>
            </div>
          </div>
          <div class="col-6">
            <div class="field mb-3">
              <label for="lastName" class="font-medium mb-1 block">Apellido</label>
              <InputText id="lastName" v-model="form.lastName" class="w-full"
                :class="{ 'p-invalid': errors.lastName }" />
              <small v-if="errors.lastName" class="p-error">{{ errors.lastName }}</small>
            </div>
          </div>
        </div>

        <div class="field mb-3">
          <label for="email" class="font-medium mb-1 block">Email</label>
          <InputText id="email" v-model="form.email" type="email" class="w-full"
            :class="{ 'p-invalid': errors.email }" />
          <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
        </div>

        <div class="grid">
          <div class="col-6">
            <div class="field mb-3">
              <label for="salary" class="font-medium mb-1 block">Salario</label>
              <InputNumber id="salary" v-model="form.salary" mode="currency" currency="USD"
                locale="en-US" class="w-full" :class="{ 'p-invalid': errors.salary }" />
              <small v-if="errors.salary" class="p-error">{{ errors.salary }}</small>
            </div>
          </div>
          <div class="col-6">
            <div class="field mb-3">
              <label for="department" class="font-medium mb-1 block">Departamento</label>
              <Select id="department" v-model="form.department" :options="departments"
                placeholder="Seleccionar departamento" class="w-full"
                :class="{ 'p-invalid': errors.department }" />
              <small v-if="errors.department" class="p-error">{{ errors.department }}</small>
            </div>
          </div>
        </div>

        <div class="field mb-3">
          <label for="hireDate" class="font-medium mb-1 block">Fecha de Contratación</label>
          <DatePicker id="hireDate" v-model="form.hireDate" dateFormat="yy-mm-dd" class="w-full"
            :class="{ 'p-invalid': errors.hireDate }" />
          <small v-if="errors.hireDate" class="p-error">{{ errors.hireDate }}</small>
        </div>

        <div class="field mb-4">
          <label class="font-medium mb-1 block">Estado</label>
          <InputSwitch v-model="form.active" />
          <span class="ml-2">{{ form.active ? 'Activo' : 'Inactivo' }}</span>
        </div>

        <div class="flex gap-2">
          <Button type="submit" label="Guardar" icon="pi pi-check" />
          <InertiaLink href="/employees" class="p-button p-component p-button-secondary">
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
  employee: Object,
  editing: Boolean,
  errors: Object
})

const departments = ['Engineering', 'Sales', 'Marketing', 'HR', 'Finance',
  'Legal', 'Operations', 'Support', 'Design', 'Product']

const form = reactive({
  firstName: props.employee?.firstName || '',
  lastName: props.employee?.lastName || '',
  email: props.employee?.email || '',
  salary: props.employee?.salary || null,
  active: props.employee?.active !== undefined ? props.employee.active : true,
  department: props.employee?.department || null,
  hireDate: props.employee?.hireDate || null
})

const errors = reactive(props.errors || {})

function toDateString(value) {
  if (value instanceof Date) {
    const y = value.getFullYear()
    const m = String(value.getMonth() + 1).padStart(2, '0')
    const d = String(value.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  return value
}

function submit() {
  const payload = { ...form }
  payload.hireDate = toDateString(payload.hireDate)
  const url = props.editing ? `/employees/${props.employee.id}` : '/employees'
  router.post(url, payload, {
    errorBag: 'employeeForm',
    onError: (errs) => {
      Object.assign(errors, errs)
    }
  })
}
</script>
