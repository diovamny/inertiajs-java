import { Head, Link, router, useForm } from '@inertiajs/react'
import type { SubmitEvent  } from 'react'
import Layout from '@/Shared/Layout'
import TextInput from '@/Shared/TextInput'
import SelectInput from '@/Shared/SelectInput'
import FileInput from '@/Shared/FileInput'
import LoadingButton from '@/Shared/LoadingButton'
import TrashedMessage from '@/Shared/TrashedMessage'
import { useSyncErrors } from '@/composables/useSyncErrors'
import type { User } from '@/types'
import type { UserForm } from './Create'

interface Props {
  user: User
}

export default function UserEdit({ user }: Props) {
  const form = useForm<UserForm & { _method: string }>({
    _method: 'put',
    first_name: user.first_name,
    last_name: user.last_name,
    email: user.email,
    password: '',
    owner: user.owner,
    photo: null,
  })
  useSyncErrors(form)

  const update = (e: SubmitEvent ) => {
    e.preventDefault()
    form.put(`/users/${user.id}`, {
        forceFormData: true,
        onSuccess: () => form.reset('password', 'photo'),
    })
  }

  const destroy = () => {
    if (confirm('Are you sure you want to delete this user?')) {
      router.delete(`/users/${user.id}`)
    }
  }

  const restore = () => {
    if (confirm('Are you sure you want to restore this user?')) {
      router.put(`/users/${user.id}/restore`)
    }
  }

  return (
    <Layout>
      <Head title={`${form.data.first_name} ${form.data.last_name}`} />
      <div className="flex justify-start mb-8 max-w-3xl">
        <h1 className="text-3xl font-bold">
          <Link className="text-indigo-400 hover:text-indigo-600" href="/users">
            Users
          </Link>
          <span className="text-indigo-400 font-medium">/</span>
          {form.data.first_name} {form.data.last_name}
        </h1>
        {user.photo && <img className="block ml-4 w-8 h-8 rounded-full" src={user.photo} />}
      </div>
      {user.deleted_at && (
        <TrashedMessage className="mb-6" onRestore={restore}>
          This user has been deleted.
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
          <div className="flex items-center px-8 py-4 bg-gray-50 border-t border-gray-100">
            {!user.deleted_at && (
              <button className="text-red-600 hover:underline" tabIndex={-1} type="button" onClick={destroy}>
                Delete User
              </button>
            )}
            <LoadingButton loading={form.processing} className="btn-indigo ml-auto" type="submit">
              Update User
            </LoadingButton>
          </div>
        </form>
      </div>
    </Layout>
  )
}
