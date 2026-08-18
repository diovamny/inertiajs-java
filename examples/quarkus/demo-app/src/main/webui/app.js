import { createInertiaApp } from '@inertiajs/vue3'
import { createApp, h } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import ConfirmDialog from 'primevue/confirmdialog'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import DatePicker from 'primevue/datepicker'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Skeleton from 'primevue/skeleton'
import Tag from 'primevue/tag'
import Toast from 'primevue/toast'
import Toolbar from 'primevue/toolbar'

import FlashMessages from './components/FlashMessages.vue'

import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'

createInertiaApp({
  progress: {
    delay: 200,
    color: '#3B82F6',
    includeCSS: true,
    showSpinner: false,
  },
  resolve: name => {
    const pages = import.meta.glob('./pages/**/*.vue', { eager: true })
    return pages[`./pages/${name}.vue`]
  },
  setup({ el, App, props, plugin }) {
    const app = createApp({
      render() {
        return h('div', [
          h(App, props),
          h(FlashMessages),
          h(ConfirmDialog),
        ])
      }
    })
    app.use(plugin)
    app.use(PrimeVue, {
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: false,
        }
      }
    })
    app.use(ToastService)
    app.use(ConfirmationService)

    app.component('ConfirmDialog', ConfirmDialog)
    app.component('Button', Button)
    app.component('Column', Column)
    app.component('DataTable', DataTable)
    app.component('DatePicker', DatePicker)
    app.component('IconField', IconField)
    app.component('InputIcon', InputIcon)
    app.component('InputNumber', InputNumber)
    app.component('InputSwitch', InputSwitch)
    app.component('InputText', InputText)
    app.component('Select', Select)
    app.component('Skeleton', Skeleton)
    app.component('Tag', Tag)
    app.component('Toast', Toast)
    app.component('Toolbar', Toolbar)

    app.mount(el)
  },
})
