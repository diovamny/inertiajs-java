import { Link } from '@inertiajs/react'
import type { PaginationLink as PaginationLinkType } from '@/types'

interface Props {
  links: PaginationLinkType[]
  className?: string
}

export default function Pagination({ links, className }: Props) {
  if (links.length <= 3) {
    return null
  }

  return (
    <div className={`flex flex-wrap -mb-1${className ? ` ${className}` : ''}`}>
      {links.map((link, key) =>
        link.url === null ? (
          <div
            key={key}
            className="mb-1 mr-1 px-4 py-3 text-gray-400 text-sm leading-4 border rounded"
            dangerouslySetInnerHTML={{ __html: link.label }}
          />
        ) : (
          <Link
            key={`link-${key}`}
            className={`mb-1 mr-1 px-4 py-3 focus:text-indigo-500 text-sm leading-4 hover:bg-white border focus:border-indigo-500 rounded${
              link.active ? ' bg-white' : ''
            }`}
            href={link.url}
            dangerouslySetInnerHTML={{ __html: link.label }}
          />
        ),
      )}
    </div>
  )
}
