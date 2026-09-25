import { useId } from 'react'

interface Props {
  id?: string
  type?: string
  error?: string
  label?: string
  value?: string | number | null
  onChange?: (value: string) => void
  className?: string
  autoFocus?: boolean
  autoComplete?: string
  autoCapitalize?: string
  placeholder?: string
  name?: string
  disabled?: boolean
  readOnly?: boolean
}

export default function TextInput({
  id,
  type = 'text',
  error,
  label,
  value,
  onChange,
  className,
  autoFocus,
  autoComplete,
  autoCapitalize,
  placeholder,
  name,
  disabled,
  readOnly,
}: Props) {
  const autoId = useId()
  const inputId = id ?? `text-input-${autoId}`
  return (
    <div className={className}>
      {label && (
        <label className="form-label" htmlFor={inputId}>
          {label}:
        </label>
      )}
      <input
        id={inputId}
        type={type}
        className={`form-input${error ? ' error' : ''}`}
        value={value ?? ''}
        onChange={(e) => onChange?.(e.target.value)}
        autoFocus={autoFocus}
        autoComplete={autoComplete}
        autoCapitalize={autoCapitalize}
        placeholder={placeholder}
        name={name}
        disabled={disabled}
        readOnly={readOnly}
      />
      {error && <div className="form-error">{error}</div>}
    </div>
  )
}
