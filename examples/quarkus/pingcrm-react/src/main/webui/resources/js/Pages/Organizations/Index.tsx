import { Head, Link, router } from '@inertiajs/react'
import { useRef, useState } from 'react'
import throttle from 'lodash/throttle'
import pickBy from 'lodash/pickBy'
import Icon from '@/Shared/Icon'
import Layout from '@/Shared/Layout'
import Pagination from '@/Shared/Pagination'
import SearchFilter from '@/Shared/SearchFilter'
import type { Filters, Organization, Paginated } from '@/types'

interface Props {
  filters: Filters
  organizations: Paginated<Organization>
}

export default function OrganizationsIndex({ filters, organizations }: Props) {
  const [form, setForm] = useState({
    search: filters.search,
    trashed: filters.trashed,
  })
  const throttled = useRef(
    throttle((data: Record<string, any>) => {
      router.get('/organizations', pickBy(data), { preserveState: true })
    }, 150),
  )

  const update = (patch: Partial<{ search: string | null; trashed: string | null }>) => {
    setForm((f) => {
      const next = { ...f, ...patch }
      throttled.current(next)
      return next
    })
  }

  const reset = () => setForm({ search: null, trashed: null })

  return (
    <Layout>
      <Head title="Organizations" />
      <h1 className="mb-8 text-3xl font-bold">Organizations</h1>
      <div className="flex items-center justify-between mb-6">
        <SearchFilter className="mr-4 w-full max-w-md" value={form.search} onChange={(search) => update({ search })} onReset={reset}>
          <label className="block text-gray-700">Trashed:</label>
          <select
            className="form-select mt-1 w-full"
            value={form.trashed ?? ''}
            onChange={(e) => update({ trashed: e.target.value === '' ? null : e.target.value })}
          >
            <option value="" />
            <option value="with">With Trashed</option>
            <option value="only">Only Trashed</option>
          </select>
        </SearchFilter>
        <Link className="btn-indigo" href="/organizations/create">
          <span>Create</span>
          <span className="hidden md:inline">&nbsp;Organization</span>
        </Link>
      </div>
      <div className="bg-white rounded-md shadow overflow-x-auto">
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
            {organizations.data.map((organization) => (
              <tr key={organization.id} className="hover:bg-gray-100 focus-within:bg-gray-100">
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4 focus:text-indigo-500" href={`/organizations/${organization.id}/edit`}>
                    {organization.name}
                    {organization.deleted_at && <Icon name="trash" className="shrink-0 ml-2 w-3 h-3 fill-gray-400" />}
                  </Link>
                </td>
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4" href={`/organizations/${organization.id}/edit`} tabIndex={-1}>
                    {organization.city}
                  </Link>
                </td>
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4" href={`/organizations/${organization.id}/edit`} tabIndex={-1}>
                    {organization.phone}
                  </Link>
                </td>
                <td className="w-px border-t">
                  <Link className="flex items-center px-4" href={`/organizations/${organization.id}/edit`} tabIndex={-1}>
                    <Icon name="cheveron-right" className="block w-6 h-6 fill-gray-400" />
                  </Link>
                </td>
              </tr>
            ))}
            {organizations.data.length === 0 && (
              <tr>
                <td className="px-6 py-4 border-t" colSpan={4}>
                  No organizations found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      <Pagination className="mt-6" links={organizations.links} />
    </Layout>
  )
}
