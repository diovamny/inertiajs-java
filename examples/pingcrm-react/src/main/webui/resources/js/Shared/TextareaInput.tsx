import { useId } from 'react'

interface Props {
  id?: string
  error?: string
  label?: string
  value?: string | null
  onChange?: (value: string) => void
  className?: string
  placeholder?: string
  name?: string
  disabled?: boolean
  rows?: number
}

export default function TextareaInput({
  id,
  error,
  label,
  value,
  onChange,
  className,
  placeholder,
  name,
  disabled,
  rows,
}: Props) {
  const autoId = useId()
  const inputId = id ?? `textarea-input-${autoId}`
  return (
    <div className={className}>
      {label && (
        <label className="form-label" htmlFor={inputId}>
          {label}:
        </label>
      )}
      <textarea
        id={inputId}
        className={`form-textarea${error ? ' error' : ''}`}
        value={value ?? ''}
        onChange={(e) => onChange?.(e.target.value)}
        placeholder={placeholder}
        name={name}
        disabled={disabled}
        rows={rows}
      />
      {error && <div className="form-error">{error}</div>}
    </div>
  )
}
