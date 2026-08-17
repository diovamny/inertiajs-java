<template>
  <div class="p-4">
    <div class="flex justify-content-between align-items-center mb-4">
      <h1 class="text-2xl font-bold">Empleados</h1>
      <InertiaLink href="/employees/create" class="p-button p-component p-button-success p-button-sm">
        <i class="pi pi-plus mr-2"></i>Nuevo Empleado
      </InertiaLink>
    </div>

    <div class="card surface-card border-round shadow-1">
      <Toolbar class="border-none">
        <template #start>
          <div class="flex gap-2 flex-wrap">
            <Button label="Exportar CSV" icon="pi pi-file-export" severity="info" text
              @click="exportCSV" />
            <Button label="Limpiar Filtros" icon="pi pi-filter-slash" severity="secondary" text
              @click="clearFilters" :disabled="!hasActiveFilters" />
          </div>
        </template>
      </Toolbar>

      <DataTable ref="dt" :value="data" :loading="loading" lazy
        :totalRecords="total" :rows="size" :first="page * size"
        v-model:filters="filters" filterDisplay="menu"
        @filter="onFilter" @sort="onSort" @page="onPage"
        stripedRows responsiveLayout="scroll" removableSort
        paginator :rowsPerPageOptions="[25, 50, 100]"
        paginatorTemplate="FirstPageLink PrevPageLink PageLinks RowsPerPageDropdown NextPageLink LastPageLink"
        currentPageReportTemplate="Mostrando {first} a {last} de {totalRecords} registros">

        <template #header>
          <div class="flex justify-content-between align-items-center">
            <span class="text-muted font-medium">Total: {{ total }} empleados</span>
          </div>
        </template>

        <template #empty>
          <div class="text-center p-4">
            <i class="pi pi-search text-4xl text-muted mb-3" style="display:block"></i>
            <p>No se encontraron empleados</p>
          </div>
        </template>

        <template #loading>
          <div class="p-4">
            <Skeleton v-for="n in 5" :key="n" class="mb-2" height="3rem" />
          </div>
        </template>

        <Column field="firstName" header="Nombre" :sortable="true" :showFilterMenu="true">
          <template #filter="{ filterModel }">
            <InputText v-model="filterModel.value" type="text" class="p-column-filter"
              placeholder="Buscar por nombre" @input="debouncedFilter" />
          </template>
          <template #body="{ data }">
            <InertiaLink :href="`/employees/${data.id}/edit`" class="text-primary font-medium">
              {{ data.firstName }} {{ data.lastName }}
            </InertiaLink>
          </template>
        </Column>

        <Column field="lastName" header="Apellido" :sortable="true" :showFilterMenu="true">
          <template #filter="{ filterModel }">
            <InputText v-model="filterModel.value" type="text" class="p-column-filter"
              placeholder="Buscar por apellido" @input="debouncedFilter" />
          </template>
        </Column>

        <Column field="email" header="Email" :sortable="true" :showFilterMenu="true">
          <template #filter="{ filterModel }">
            <InputText v-model="filterModel.value" type="text" class="p-column-filter"
              placeholder="Buscar por email" @input="debouncedFilter" />
          </template>
        </Column>

        <Column field="salary" header="Salario" :sortable="true" dataType="numeric" :showFilterMenu="true">
          <template #filter="{ filterModel }">
            <div class="flex gap-2">
              <InputNumber v-model="filterModel.value[0]" placeholder="Mín" mode="currency"
                currency="USD" locale="en-US" class="w-full" @input="debouncedFilter" />
              <InputNumber v-model="filterModel.value[1]" placeholder="Máx" mode="currency"
                currency="USD" locale="en-US" class="w-full" @input="debouncedFilter" />
            </div>
          </template>
          <template #body="{ data }">
            {{ formatCurrency(data.salary) }}
          </template>
        </Column>

        <Column field="department" header="Departamento" :sortable="true" :showFilterMenu="true">
          <template #filter="{ filterModel }">
            <InputText v-model="filterModel.value" type="text" class="p-column-filter"
              placeholder="Buscar por departamento" @input="debouncedFilter" />
          </template>
        </Column>

        <Column field="active" header="Activo" :sortable="true" dataType="boolean" :showFilterMenu="true">
          <template #filter="{ filterModel }">
            <Select v-model="filterModel.value" :options="activeOptions" optionLabel="label"
              optionValue="value" placeholder="Estado" class="w-full"
              @change="debouncedFilter" />
          </template>
          <template #body="{ data }">
            <Tag :value="data.active ? 'Activo' : 'Inactivo'"
              :severity="data.active ? 'success' : 'danger'" />
          </template>
        </Column>

        <Column header="Acciones" style="width:120px">
          <template #body="{ data }">
            <InertiaLink :href="`/employees/${data.id}/edit`"
              class="p-button p-button-sm p-button-text p-button-rounded mr-1"
              :title="'Editar ' + data.firstName">
              <i class="pi pi-pencil"></i>
            </InertiaLink>
            <Button icon="pi pi-trash" severity="danger" text rounded size="small"
              @click="confirmDelete(data)" />
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { router, Link as InertiaLink } from '@inertiajs/vue3'
import { useConfirm } from 'primevue/useconfirm'
import { FilterMatchMode } from '@primevue/core/api'

const props = defineProps({
  data: Array,
  total: Number,
  page: Number,
  size: Number
})

const dt = ref(null)
const confirm = useConfirm()
const loading = ref(false)
const page = ref(props.page || 0)
const size = ref(props.size || 50)
const sortField = ref(null)
const sortOrder = ref(1)

const activeOptions = [
  { label: 'Todos', value: null },
  { label: 'Activos', value: true },
  { label: 'Inactivos', value: false }
]

const filters = ref({
  firstName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  lastName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  email: { value: null, matchMode: FilterMatchMode.CONTAINS },
  salary: { value: [null, null], matchMode: FilterMatchMode.BETWEEN },
  department: { value: null, matchMode: FilterMatchMode.CONTAINS },
  active: { value: null, matchMode: FilterMatchMode.EQUALS }
})

const hasActiveFilters = computed(() => {
  return Object.values(filters.value).some(f =>
    f.value !== null && f.value !== '' && f.value !== undefined
  )
})

let filterTimeout = null
function debouncedFilter() {
  clearTimeout(filterTimeout)
  filterTimeout = setTimeout(loadData, 400)
}

function loadData() {
  loading.value = true
  const filterPayload = {}
  for (const [key, f] of Object.entries(filters.value)) {
    if (f.value !== null && f.value !== '' && f.value !== undefined) {
      filterPayload[key] = { value: f.value, matchMode: f.matchMode }
    }
  }

  router.get('/employees', {
    filters: JSON.stringify(filterPayload),
    sortField: sortField.value,
    sortOrder: sortOrder.value,
    page: page.value,
    size: size.value
  }, {
    preserveState: true,
    preserveScroll: true,
    onFinish: () => { loading.value = false }
  })
}

function onFilter(event) {
  page.value = 0
  loadData()
}

function onSort(event) {
  sortField.value = event.sortField
  sortOrder.value = event.sortOrder
  loadData()
}

function onPage(event) {
  page.value = event.page
  size.value = event.rows
  loadData()
}

function exportCSV() {
  dt.value.exportCSV()
}

function clearFilters() {
  for (const key of Object.keys(filters.value)) {
    const f = filters.value[key]
    if (key === 'salary') {
      f.value = [null, null]
    } else {
      f.value = null
    }
  }
  page.value = 0
  loadData()
}

function formatCurrency(value) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
}

function confirmDelete(emp) {
  confirm.require({
    message: `¿Eliminar a ${emp.firstName} ${emp.lastName}?`,
    header: 'Confirmar eliminación',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Cancelar',
    acceptLabel: 'Eliminar',
    accept: () => {
      router.post(`/employees/${emp.id}/delete`)
    }
  })
}
</script>
