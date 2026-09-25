import { Head, Link, router } from '@inertiajs/react'
import { useRef, useState } from 'react'
import throttle from 'lodash/throttle'
import pickBy from 'lodash/pickBy'
import Icon from '@/Shared/Icon'
import Layout from '@/Shared/Layout'
import SearchFilter from '@/Shared/SearchFilter'
import type { User, UsersFilters } from '@/types'

interface Props {
  filters: UsersFilters
  users: User[]
}

export default function UsersIndex({ filters, users }: Props) {
  const [form, setForm] = useState({
    search: filters.search,
    role: filters.role,
    trashed: filters.trashed,
  })
  const throttled = useRef(
    throttle((data: Record<string, any>) => {
      router.get('/users', pickBy(data), { preserveState: true })
    }, 150),
  )

  const update = (patch: Partial<{ search: string | null; role: string | null; trashed: string | null }>) => {
    setForm((f) => {
      const next = { ...f, ...patch }
      throttled.current(next)
      return next
    })
  }

  const reset = () => setForm({ search: null, role: null, trashed: null })

  return (
    <Layout>
      <Head title="Users" />
      <h1 className="mb-8 text-3xl font-bold">Users</h1>
      <div className="flex items-center justify-between mb-6">
        <SearchFilter className="mr-4 w-full max-w-md" value={form.search} onChange={(search) => update({ search })} onReset={reset}>
          <label className="block text-gray-700">Role:</label>
          <select
            className="form-select mt-1 w-full"
            value={form.role ?? ''}
            onChange={(e) => update({ role: e.target.value === '' ? null : e.target.value })}
          >
            <option value="" />
            <option value="user">User</option>
            <option value="owner">Owner</option>
          </select>
          <label className="block mt-4 text-gray-700">Trashed:</label>
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
        <Link className="btn-indigo" href="/users/create">
          <span>Create</span>
          <span className="hidden md:inline">&nbsp;User</span>
        </Link>
      </div>
      <div className="bg-white rounded-md shadow overflow-x-auto">
        <table className="w-full whitespace-nowrap">
          <thead>
            <tr className="text-left font-bold">
              <th className="pb-4 pt-6 px-6">Name</th>
              <th className="pb-4 pt-6 px-6">Email</th>
              <th className="pb-4 pt-6 px-6" colSpan={2}>
                Role
              </th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-100 focus-within:bg-gray-100">
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4 focus:text-indigo-500" href={`/users/${user.id}/edit`}>
                    {user.photo && <img className="block -my-2 mr-2 w-5 h-5 rounded-full" src={user.photo} />}
                    {user.name}
                    {user.deleted_at && <Icon name="trash" className="shrink-0 ml-2 w-3 h-3 fill-gray-400" />}
                  </Link>
                </td>
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4" href={`/users/${user.id}/edit`} tabIndex={-1}>
                    {user.email}
                  </Link>
                </td>
                <td className="border-t">
                  <Link className="flex items-center px-6 py-4" href={`/users/${user.id}/edit`} tabIndex={-1}>
                    {user.owner ? 'Owner' : 'User'}
                  </Link>
                </td>
                <td className="w-px border-t">
                  <Link className="flex items-center px-4" href={`/users/${user.id}/edit`} tabIndex={-1}>
                    <Icon name="cheveron-right" className="block w-6 h-6 fill-gray-400" />
                  </Link>
                </td>
              </tr>
            ))}
            {users.length === 0 && (
              <tr>
                <td className="px-6 py-4 border-t" colSpan={4}>
                  No users found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  )
}
