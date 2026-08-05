import type { ReactNode } from 'react'
import Icon from '@/Shared/Icon'

interface Props {
  className?: string
  onRestore: () => void
  children?: ReactNode
}

export default function TrashedMessage({ className, onRestore, children }: Props) {
  return (
    <div className={`flex items-center justify-between p-4 max-w-3xl bg-yellow-400 rounded${className ? ` ${className}` : ''}`}>
      <div className="flex items-center">
        <Icon name="trash" className="shrink-0 mr-2 w-4 h-4 fill-yellow-800" />
        <div className="text-yellow-800 text-sm font-medium">{children}</div>
      </div>
      <button className="text-yellow-800 hover:underline text-sm" tabIndex={-1} type="button" onClick={onRestore}>
        Restore
      </button>
    </div>
  )
}
