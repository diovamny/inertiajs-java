import { Head, useForm } from '@inertiajs/react'
import type { FormEvent } from 'react'
import Logo from '@/Shared/Logo'
import TextInput from '@/Shared/TextInput'
import LoadingButton from '@/Shared/LoadingButton'
import { useSyncErrors } from '@/composables/useSyncErrors'

interface LoginForm {
  email: string
  password: string
  remember: boolean
}

export default function Login() {
  const form = useForm<LoginForm>({
    email: 'johndoe@example.com',
    password: 'secret',
    remember: false,
  })
  useSyncErrors(form)

  const login = (e: FormEvent) => {
    e.preventDefault()
    form.post('/login')
  }

  return (
    <div>
      <Head title="Login" />
      <div className="flex items-center justify-center p-6 min-h-screen bg-indigo-800">
        <div className="w-full max-w-md">
          <Logo className="block mx-auto w-full max-w-xs fill-white" height={50} />
          <form className="mt-8 bg-white rounded-lg shadow-xl overflow-hidden" onSubmit={login}>
            <div className="px-10 py-12">
              <h1 className="text-center text-3xl font-bold">Welcome Back!</h1>
              <div className="mt-6 mx-auto w-24 border-b-2" />
              <TextInput
                value={form.data.email}
                onChange={(email) => form.setData('email', email)}
                error={form.errors.email}
                className="mt-10"
                label="Email"
                type="email"
                autoFocus
                autoCapitalize="off"
              />
              <TextInput
                value={form.data.password}
                onChange={(password) => form.setData('password', password)}
                error={form.errors.password}
                className="mt-6"
                label="Password"
                type="password"
              />
              <label className="flex items-center mt-6 select-none" htmlFor="remember">
                <input
                  id="remember"
                  className="mr-1"
                  type="checkbox"
                  checked={form.data.remember}
                  onChange={(e) => form.setData('remember', e.target.checked)}
                />
                <span className="text-sm">Remember Me</span>
              </label>
            </div>
            <div className="flex px-10 py-4 bg-gray-100 border-t border-gray-100">
              <LoadingButton loading={form.processing} className="btn-indigo ml-auto" type="submit">
                Login
              </LoadingButton>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
