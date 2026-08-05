import { Head, Link, router, useForm } from '@inertiajs/react'
import type { FormEvent } from 'react'
import Icon from '@/Shared/Icon'
import Layout from '@/Shared/Layout'
import TextInput from '@/Shared/TextInput'
import SelectInput from '@/Shared/SelectInput'
import LoadingButton from '@/Shared/LoadingButton'
import TrashedMessage from '@/Shared/TrashedMessage'
import { useSyncErrors } from '@/composables/useSyncErrors'
import type { OrganizationWithContacts } from '@/types'
import type { OrganizationForm } from './Create'

interface Props {
  organization: OrganizationWithContacts
}

export default function OrganizationEdit({ organization }: Props) {
  const form = useForm<OrganizationForm>({
    name: organization.name,
    email: organization.email,
    phone: organization.phone,
    address: organization.address,
    city: organization.city,
    region: organization.region,
    country: organization.country,
    postal_code: organization.postal_code,
  })
  useSyncErrors(form)

  const update = (e: FormEvent) => {
    e.preventDefault()
    form.put(`/organizations/${organization.id}`)
  }

  const destroy = () => {
    if (confirm('Are you sure you want to delete this organization?')) {
      router.delete(`/organizations/${organization.id}`)
    }
  }

  const restore = () => {
    if (confirm('Are you sure you want to restore this organization?')) {
      router.put(`/organizations/${organization.id}/restore`)
    }
  }

  return (
    <Layout>
      <Head title={form.data.name ?? ''} />
      <h1 className="mb-8 text-3xl font-bold">
        <Link className="text-indigo-400 hover:text-indigo-600" href="/organizations">
          Organizations
        </Link>
        <span className="text-indigo-400 font-medium">/</span>
        {form.data.name}
      </h1>
      {organization.deleted_at && (
        <TrashedMessage className="mb-6" onRestore={restore}>
          This organization has been deleted.
        </TrashedMessage>
      )}
      <div className="max-w-3xl bg-white rounded-md shadow overflow-hidden">
        <form onSubmit={update}>
          <div className="flex flex-wrap -mb-8 -mr-6 p-8">
            <TextInput
              value={form.data.name}
              onChange={(name) => form.setData('name', name)}
              error={form.errors.name}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Name"
            />
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
            {!organization.deleted_at && (
              <button className="text-red-600 hover:underline" tabIndex={-1} type="button" onClick={destroy}>
                Delete Organization
              </button>
            )}
            <LoadingButton loading={form.processing} className="btn-indigo ml-auto" type="submit">
              Update Organization
            </LoadingButton>
          </div>
        </form>
      </div>
      <h2 className="mt-12 text-2xl font-bold">Contacts</h2>
      <div className="mt-6 bg-white rounded shadow overflow-x-auto">
        <table className="w-full whitespace-nowrap">
          <thead>
            <tr className="text-left font-bold">
              <th className="pb-4 pt-6 px-6">Name</th>
              <th className="pb-4 pt-6 px-6">City</th>
              <th className="pb-4 pt-6 px-6" colSpan={2}>
                Phone
              </th>
            </tr>
          </thead>
          <tbody>
            {organization.contacts.map((contact) => (
              <tr key={contact.id} className="hover:bg-gray-100 focus-within:bg-gray-100">
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4 focus:text-indigo-500" href={`/contacts/${contact.id}/edit`}>
                    {contact.name}
                    {contact.deleted_at && <Icon name="trash" className="shrink-0 ml-2 w-3 h-3 fill-gray-400" />}
                  </Link>
                </td>
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4" href={`/contacts/${contact.id}/edit`} tabIndex={-1}>
                    {contact.city}
                  </Link>
                </td>
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4" href={`/contacts/${contact.id}/edit`} tabIndex={-1}>
                    {contact.phone}
                  </Link>
                </td>
                <td className="w-px border-t">
                  <Link className="flex items-center px-4" href={`/contacts/${contact.id}/edit`} tabIndex={-1}>
                    <Icon name="cheveron-right" className="block w-6 h-6 fill-gray-400" />
                  </Link>
                </td>
              </tr>
            ))}
            {organization.contacts.length === 0 && (
              <tr>
                <td className="px-6 py-4 border-t" colSpan={4}>
                  No contacts found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  )
}
