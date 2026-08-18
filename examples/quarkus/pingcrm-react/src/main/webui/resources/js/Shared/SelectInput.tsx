import { useId, type ReactNode } from 'react'

interface Props {
  id?: string
  error?: string
  label?: string
  value?: string | number | boolean | null
  onChange?: (value: string) => void
  className?: string
  children?: ReactNode
  disabled?: boolean
  name?: string
}

export default function SelectInput({
  id,
  error,
  label,
  value,
  onChange,
  className,
  children,
  disabled,
  name,
}: Props) {
  const autoId = useId()
  const inputId = id ?? `select-input-${autoId}`
  return (
    <div className={className}>
      {label && (
        <label className="form-label" htmlFor={inputId}>
          {label}:
        </label>
      )}
      <select
        id={inputId}
        className={`form-select${error ? ' error' : ''}`}
        value={value === null || value === undefined ? '' : String(value)}
        onChange={(e) => onChange?.(e.target.value)}
        disabled={disabled}
        name={name}
      >
        {children}
      </select>
      {error && <div className="form-error">{error}</div>}
    </div>
  )
}
