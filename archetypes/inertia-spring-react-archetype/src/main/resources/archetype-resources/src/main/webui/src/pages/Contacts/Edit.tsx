import type { FormEvent } from 'react'
import { Head, Link, useForm } from '@inertiajs/react'
import Layout from '../Layout'

type Contact = { id: number; name: string; email: string; phone: string | null }

export default function ContactsEdit({ contact }: { contact: Contact }) {
  const form = useForm({
    name: contact.name,
    email: contact.email,
    phone: contact.phone ?? '',
  })

  const submit = (event: FormEvent) => {
    event.preventDefault()
    form.put(`/contacts/${contact.id}`, { preserveScroll: true })
  }

  return (
    <Layout>
      <Head title={`Editar ${contact.name}`} />
      <h1>Editar contacto</h1>
      <form className="form" onSubmit={submit}>
        <label>
          <span>Nombre</span>
          <input
            type="text"
            autoComplete="name"
            value={form.data.name}
            onChange={(event) => form.setData('name', event.target.value)}
          />
          {form.errors.name && <span className="error">{form.errors.name}</span>}
        </label>
        <label>
          <span>Email</span>
          <input
            type="email"
            autoComplete="email"
            value={form.data.email}
            onChange={(event) => form.setData('email', event.target.value)}
          />
          {form.errors.email && <span className="error">{form.errors.email}</span>}
        </label>
        <label>
          <span>Teléfono</span>
          <input
            type="text"
            autoComplete="tel"
            value={form.data.phone}
            onChange={(event) => form.setData('phone', event.target.value)}
          />
          {form.errors.phone && <span className="error">{form.errors.phone}</span>}
        </label>
        <div className="form-actions">
          <button className="button" type="submit" disabled={form.processing}>
            Guardar cambios
          </button>
          <Link href="/contacts">Cancelar</Link>
        </div>
      </form>
    </Layout>
  )
}
