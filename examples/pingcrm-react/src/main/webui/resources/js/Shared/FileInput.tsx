import { useEffect, useRef } from 'react'

interface Props {
  value?: File | null
  label?: string
  accept?: string
  error?: string
  errors?: string[]
  onChange?: (file: File | null) => void
  className?: string
}

function filesize(size: number) {
  const i = Math.floor(Math.log(size) / Math.log(1024))
  return Number((size / Math.pow(1024, i)).toFixed(2)) + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i]
}

export default function FileInput({ value, label, accept, error, errors = [], onChange, className }: Props) {
  const fileRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (!value && fileRef.current) {
      fileRef.current.value = ''
    }
  }, [value])

  return (
    <div className={className}>
      {label && <div className="form-label">{label}:</div>}
      <div className={`form-input p-0${error || errors.length ? ' error' : ''}`}>
        <input
          ref={fileRef}
          type="file"
          accept={accept}
          className="hidden"
          onChange={(e) => onChange?.(e.target.files?.[0] ?? null)}
        />
        {!value ? (
          <div className="p-2">
            <button
              type="button"
              className="px-4 py-1 text-white text-xs font-medium bg-gray-500 hover:bg-gray-700 rounded-sm"
              onClick={() => fileRef.current?.click()}
            >
              Browse
            </button>
          </div>
        ) : (
          <div className="flex items-center justify-between p-2">
            <div className="flex-1 pr-1">
              {value.name} <span className="text-gray-500 text-xs">({filesize(value.size)})</span>
            </div>
            <button
              type="button"
              className="px-4 py-1 text-white text-xs font-medium bg-gray-500 hover:bg-gray-700 rounded-sm"
              onClick={() => onChange?.(null)}
            >
              Remove
            </button>
          </div>
        )}
      </div>
      {(error || errors.length > 0) && <div className="form-error">{errors[0] ?? error}</div>}
    </div>
  )
}
