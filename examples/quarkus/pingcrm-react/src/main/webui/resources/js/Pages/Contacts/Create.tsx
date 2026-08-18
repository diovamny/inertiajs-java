import { Head, Link, useForm } from '@inertiajs/react'
import type { FormEvent } from 'react'
import Layout from '@/Shared/Layout'
import TextInput from '@/Shared/TextInput'
import SelectInput from '@/Shared/SelectInput'
import LoadingButton from '@/Shared/LoadingButton'
import { useSyncErrors } from '@/composables/useSyncErrors'
import type { OrganizationSummary } from '@/types'

export interface ContactForm {
  first_name: string | null
  last_name: string | null
  organization_id: number | null
  email: string | null
  phone: string | null
  address: string | null
  city: string | null
  region: string | null
  country: string | null
  postal_code: string | null
}

interface Props {
  organizations: OrganizationSummary[]
}

export default function ContactCreate({ organizations }: Props) {
  const form = useForm<ContactForm>({
    first_name: '',
    last_name: '',
    organization_id: null,
    email: '',
    phone: '',
    address: '',
    city: '',
    region: '',
    country: '',
    postal_code: '',
  })
  useSyncErrors(form)

  const store = (e: FormEvent) => {
    e.preventDefault()
    form.post('/contacts')
  }

  return (
    <Layout>
      <Head title="Create Contact" />
      <h1 className="mb-8 text-3xl font-bold">
        <Link className="text-indigo-400 hover:text-indigo-600" href="/contacts">
          Contacts
        </Link>
        <span className="text-indigo-400 font-medium">/</span> Create
      </h1>
      <div className="max-w-3xl bg-white rounded-md shadow overflow-hidden">
        <form onSubmit={store}>
          <div className="flex flex-wrap -mb-8 -mr-6 p-8">
            <TextInput
              value={form.data.first_name}
              onChange={(first_name) => form.setData('first_name', first_name)}
              error={form.errors.first_name}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="First name"
            />
            <TextInput
              value={form.data.last_name}
              onChange={(last_name) => form.setData('last_name', last_name)}
              error={form.errors.last_name}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Last name"
            />
            <SelectInput
              value={form.data.organization_id}
              onChange={(value) => form.setData('organization_id', value === '' ? null : Number(value))}
              error={form.errors.organization_id}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Organization"
            >
              <option value="" />
              {organizations.map((organization) => (
                <option key={organization.id} value={organization.id}>
                  {organization.name}
                </option>
              ))}
            </SelectInput>
            <TextInput
              value={form.data.email}
              onChange={(email) => form.setData('email', email)}
              error={form.errors.email}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Email"
            />
            <TextInput
              value={form.data.phone}
              onChange={(phone) => form.setData('phone', phone)}
              error={form.errors.phone}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Phone"
            />
            <TextInput
              value={form.data.address}
              onChange={(address) => form.setData('address', address)}
              error={form.errors.address}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Address"
            />
            <TextInput
              value={form.data.city}
              onChange={(city) => form.setData('city', city)}
              error={form.errors.city}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="City"
            />
            <TextInput
              value={form.data.region}
              onChange={(region) => form.setData('region', region)}
              error={form.errors.region}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Province/State"
            />
            <SelectInput
              value={form.data.country}
              onChange={(country) => form.setData('country', country === '' ? null : country)}
              error={form.errors.country}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Country"
            >
              <option value="" />
              <option value="CA">Canada</option>
              <option value="US">United States</option>
            </SelectInput>
            <TextInput
              value={form.data.postal_code}
              onChange={(postal_code) => form.setData('postal_code', postal_code)}
              error={form.errors.postal_code}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Postal code"
            />
          </div>
          <div className="flex items-center justify-end px-8 py-4 bg-gray-50 border-t border-gray-100">
            <LoadingButton loading={form.processing} className="btn-indigo" type="submit">
              Create Contact
            </LoadingButton>
          </div>
        </form>
      </div>
    </Layout>
  )
}
