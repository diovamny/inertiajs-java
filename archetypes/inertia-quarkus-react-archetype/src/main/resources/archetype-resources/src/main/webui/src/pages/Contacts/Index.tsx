import { Head, Link, router } from '@inertiajs/react'
import Layout from '../Layout'

type Contact = { id: number; name: string; email: string; phone: string | null }

export default function ContactsIndex({ contacts }: { contacts: Contact[] }) {
  const destroy = (contact: Contact) => {
    if (!confirm(`¿Eliminar el contacto ${contact.name}?`)) return
    router.delete(`/contacts/${contact.id}`, { preserveScroll: true })
  }

  return (
    <Layout>
      <Head title="Contactos" />
      <div className="page-header">
        <h1>Contactos</h1>
        <Link className="button" href="/contacts/create">
          Nuevo contacto
        </Link>
      </div>
      <table className="table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Email</th>
            <th>Teléfono</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {contacts.map((contact) => (
            <tr key={contact.id}>
              <td>
                <Link href={`/contacts/${contact.id}/edit`}>{contact.name}</Link>
              </td>
              <td>{contact.email}</td>
              <td>{contact.phone ?? ''}</td>
              <td>
                <div className="actions">
                  <Link href={`/contacts/${contact.id}/edit`}>Editar</Link>
                  <button type="button" className="button danger" onClick={() => destroy(contact)}>
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          ))}
          {contacts.length === 0 && (
            <tr>
              <td colSpan={4}>No hay contactos todavía.</td>
            </tr>
          )}
        </tbody>
      </table>
    </Layout>
  )
}
