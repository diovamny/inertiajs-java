import type { ReactNode } from 'react'

interface Props {
  loading?: boolean
  className?: string
  type?: 'button' | 'submit' | 'reset'
  children?: ReactNode
}

export default function LoadingButton({ loading = false, className, type = 'button', children }: Props) {
  return (
    <button type={type} disabled={loading} className={`flex items-center${className ? ` ${className}` : ''}`}>
      {loading && <div className="btn-spinner mr-2" />}
      {children}
    </button>
  )
}
