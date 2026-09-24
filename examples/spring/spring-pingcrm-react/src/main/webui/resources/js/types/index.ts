export interface Account {
  id: number | null
  name: string | null
}

export interface AuthUser {
  id: number
  first_name: string
  last_name: string
  email: string
  owner: boolean
  account: Account
}

export interface Auth {
  user: AuthUser | null
}

export interface PageProps extends Record<string, any> {
  auth: Auth
  success: string | null
  error: string | null
  errors: Record<string, string>
}

export interface OrganizationSummary {
  id: number
  name: string
}

export interface PaginationLink {
  url: string | null
  label: string
  active: boolean
}

export interface Paginated<T> {
  data: T[]
  links: PaginationLink[]
}

export interface Organization {
  id: number
  name: string
  email: string
  phone: string
  address: string
  city: string
  region: string
  country: string
  postal_code: string
  deleted_at: string | null
  contacts_count: number | null
}

export interface ContactSummary {
  id: number
  name: string
  city: string
  phone: string
  deleted_at: string | null
}

export interface OrganizationWithContacts extends Organization {
  contacts: ContactSummary[]
}

export interface Contact {
  id: number
  name: string
  first_name: string
  last_name: string
  organization_id: number | null
  email: string
  phone: string
  address: string
  city: string
  region: string
  country: string
  postal_code: string
  deleted_at: string | null
  organization: OrganizationSummary | null
}

export interface User {
  id: number
  name: string
  first_name: string
  last_name: string
  email: string
  owner: boolean
  photo: string | null
  deleted_at: string | null
}

export interface Filters {
  search: string | null
  trashed: string | null
}

export interface UsersFilters extends Filters {
  role: string | null
}
