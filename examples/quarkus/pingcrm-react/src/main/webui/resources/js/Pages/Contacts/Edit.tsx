import { Head, Link, router, useForm } from '@inertiajs/react'
import type { FormEvent } from 'react'
import Layout from '@/Shared/Layout'
import TextInput from '@/Shared/TextInput'
import SelectInput from '@/Shared/SelectInput'
import LoadingButton from '@/Shared/LoadingButton'
import TrashedMessage from '@/Shared/TrashedMessage'
import { useSyncErrors } from '@/composables/useSyncErrors'
import type { Contact, OrganizationSummary } from '@/types'
import type { ContactForm } from './Create'

interface Props {
  contact: Contact
  organizations: OrganizationSummary[]
}

export default function ContactEdit({ contact, organizations }: Props) {
  const form = useForm<ContactForm>({
    first_name: contact.first_name,
    last_name: contact.last_name,
    organization_id: contact.organization_id,
    email: contact.email,
    phone: contact.phone,
    address: contact.address,
    city: contact.city,
    region: contact.region,
    country: contact.country,
    postal_code: contact.postal_code,
  })
  useSyncErrors(form)

  const update = (e: FormEvent) => {
    e.preventDefault()
    form.put(`/contacts/${contact.id}`)
  }

  const destroy = () => {
    if (confirm('Are you sure you want to delete this contact?')) {
      router.delete(`/contacts/${contact.id}`)
    }
  }

  const restore = () => {
    if (confirm('Are you sure you want to restore this contact?')) {
      router.put(`/contacts/${contact.id}/restore`)
    }
  }

  return (
    <Layout>
      <Head title={`${form.data.first_name} ${form.data.last_name}`} />
      <h1 className="mb-8 text-3xl font-bold">
        <Link className="text-indigo-400 hover:text-indigo-600" href="/contacts">
          Contacts
        </Link>
        <span className="text-indigo-400 font-medium">/</span>
        {form.data.first_name} {form.data.last_name}
      </h1>
      {contact.deleted_at && (
        <TrashedMessage className="mb-6" onRestore={restore}>
          This contact has been deleted.
        </TrashedMessage>
      )}
      <div className="max-w-3xl bg-white rounded-md shadow overflow-hidden">
        <form onSubmit={update}>
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
          <div className="flex items-center px-8 py-4 bg-gray-50 border-t border-gray-100">
            {!contact.deleted_at && (
              <button className="text-red-600 hover:underline" tabIndex={-1} type="button" onClick={destroy}>
                Delete Contact
              </button>
            )}
            <LoadingButton loading={form.processing} className="btn-indigo ml-auto" type="submit">
              Update Contact
            </LoadingButton>
          </div>
        </form>
      </div>
    </Layout>
  )
}
