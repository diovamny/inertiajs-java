<template>
  <div class="p-4">
    <div class="flex justify-content-between align-items-center mb-4">
      <h1 class="text-2xl font-bold">Personas</h1>
      <InertiaLink href="/persons/create" class="p-button p-component p-button-success p-button-sm">
        <i class="pi pi-plus mr-2"></i>Nueva Persona
      </InertiaLink>
    </div>

    <div class="card p-3 mb-4 surface-ground border-round">
      <div class="flex gap-3 flex-wrap">
        <IconField iconPosition="left">
          <InputIcon class="pi pi-search" />
          <InputText v-model="search" placeholder="Buscar..." @input="onSearch" />
        </IconField>
        <Select v-model="activeFilter" :options="activeOptions" optionLabel="label" optionValue="value"
          placeholder="Estado" @change="onFilterChange" class="w-8rem" />
      </div>
    </div>

    <DataTable :value="persons" :loading="isFetching" lazy stripedRows responsiveLayout="scroll"
      :totalRecords="total" :rows="size" :first="(page - 1) * size"
      @sort="onSort" @page="onPageChange" paginator
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink">
      <Column field="name" header="Nombre" :sortable="true">
        <template #body="{ data }">
          <InertiaLink :href="`/persons/${data.id}/edit`" class="text-primary font-medium">
            {{ data.name }}
          </InertiaLink>
        </template>
      </Column>
      <Column field="lastName" header="Apellido" :sortable="true" />
      <Column field="email" header="Email" :sortable="true" />
      <Column field="phone" header="Teléfono" :sortable="true" />
      <Column field="active" header="Activo" :sortable="true" dataType="boolean">
        <template #body="{ data }">
          <Tag :value="data.active ? 'Activo' : 'Inactivo'"
            :severity="data.active ? 'success' : 'danger'" />
        </template>
      </Column>
      <Column header="Acciones" style="width:120px">
        <template #body="{ data }">
          <InertiaLink :href="`/persons/${data.id}/edit`"
            class="p-button p-button-sm p-button-text p-button-rounded mr-1"
            :title="'Editar ' + data.name">
            <i class="pi pi-pencil"></i>
          </InertiaLink>
          <Button icon="pi pi-trash" severity="danger" text rounded size="small"
            @click="confirmDelete(data)" />
        </template>
        </Column>
    </DataTable>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { router, Link as InertiaLink } from '@inertiajs/vue3'
import { useConfirm } from 'primevue/useconfirm'

const props = defineProps({
  persons: Array,
  total: Number,
  page: Number,
  size: Number,
  search: String,
  active: String,
  sort: String,
  order: String
})

const confirm = useConfirm()
const isFetching = ref(false)
const search = ref(props.search || '')
const activeFilter = ref(props.active || '')
let searchTimeout = null

const activeOptions = [
  { label: 'Todos', value: '' },
  { label: 'Activos', value: 'true' },
  { label: 'Inactivos', value: 'false' }
]

function navigate(params) {
  isFetching.value = true
  router.get('/persons', { ...params }, {
    preserveState: true,
    preserveScroll: true,
    onFinish: () => { isFetching.value = false }
  })
}

function onSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    navigate({
      search: search.value,
      active: activeFilter.value,
      page: 1
    })
  }, 300)
}

function onFilterChange() {
  navigate({ search: search.value, active: activeFilter.value, page: 1 })
}

function onSort(event) {
  navigate({
    search: search.value,
    active: activeFilter.value,
    sort: event.sortField,
    order: event.sortOrder === -1 ? 'desc' : 'asc',
    page: 1
  })
}

function onPageChange(event) {
  navigate({
    search: search.value,
    active: activeFilter.value,
    page: event.page + 1
  })
}

function confirmDelete(person) {
  confirm.require({
    message: `¿Eliminar a ${person.name} ${person.lastName}?`,
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    accept: () => {
      router.post(`/persons/${person.id}/delete`)
    }
  })
}
</script>
