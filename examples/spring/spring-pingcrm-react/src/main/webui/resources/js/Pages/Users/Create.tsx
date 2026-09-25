import { Head, Link, useForm } from '@inertiajs/react'
import type { FormEvent } from 'react'
import Layout from '@/Shared/Layout'
import TextInput from '@/Shared/TextInput'
import SelectInput from '@/Shared/SelectInput'
import FileInput from '@/Shared/FileInput'
import LoadingButton from '@/Shared/LoadingButton'
import { useSyncErrors } from '@/composables/useSyncErrors'

export interface UserForm {
  first_name: string
  last_name: string
  email: string
  password: string
  owner: boolean
  photo: File | null
}

export default function UserCreate() {
  const form = useForm<UserForm>({
    first_name: '',
    last_name: '',
    email: '',
    password: '',
    owner: false,
    photo: null,
  })
  useSyncErrors(form)

  const store = (e: FormEvent) => {
    e.preventDefault()
    form.post('/users')
  }

  return (
    <Layout>
      <Head title="Create User" />
      <h1 className="mb-8 text-3xl font-bold">
        <Link className="text-indigo-400 hover:text-indigo-600" href="/users">
          Users
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
            <TextInput
              value={form.data.email}
              onChange={(email) => form.setData('email', email)}
              error={form.errors.email}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Email"
            />
            <TextInput
              value={form.data.password}
              onChange={(password) => form.setData('password', password)}
              error={form.errors.password}
              className="pb-8 pr-6 w-full lg:w-1/2"
              type="password"
              autoComplete="new-password"
              label="Password"
            />
            <SelectInput
              value={form.data.owner}
              onChange={(value) => form.setData('owner', value === 'true')}
              error={form.errors.owner}
              className="pb-8 pr-6 w-full lg:w-1/2"
              label="Owner"
            >
              <option value="true">Yes</option>
              <option value="false">No</option>
            </SelectInput>
            <FileInput
              value={form.data.photo}
              onChange={(photo) => form.setData('photo', photo)}
              error={form.errors.photo}
              className="pb-8 pr-6 w-full lg:w-1/2"
              accept="image/*"
              label="Photo"
            />
          </div>
          <div className="flex items-center justify-end px-8 py-4 bg-gray-50 border-t border-gray-100">
            <LoadingButton loading={form.processing} className="btn-indigo" type="submit">
              Create User
            </LoadingButton>
          </div>
        </form>
      </div>
    </Layout>
  )
}
