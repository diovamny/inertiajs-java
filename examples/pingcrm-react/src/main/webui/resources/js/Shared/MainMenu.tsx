import { Link, usePage } from '@inertiajs/react'
import Icon from '@/Shared/Icon'
import type { PageProps } from '@/types'

export default function MainMenu() {
  const { url } = usePage<PageProps>()

  const isUrl = (...urls: string[]) => {
    const currentUrl = url.substr(1)
    if (urls[0] === '') {
      return currentUrl === ''
    }
    return urls.some((u) => currentUrl.startsWith(u))
  }

  return (
    <div>
      <div className="mb-4">
        <Link className="group flex items-center py-3" href="/">
          <Icon
            name="dashboard"
            className={`mr-2 w-4 h-4 ${isUrl('') ? 'fill-white' : 'fill-indigo-400 group-hover:fill-white'}`}
          />
          <div className={isUrl('') ? 'text-white' : 'text-indigo-300 group-hover:text-white'}>Dashboard</div>
        </Link>
      </div>
      <div className="mb-4">
        <Link className="group flex items-center py-3" href="/organizations">
          <Icon
            name="office"
            className={`mr-2 w-4 h-4 ${isUrl('organizations') ? 'fill-white' : 'fill-indigo-400 group-hover:fill-white'}`}
          />
          <div className={isUrl('organizations') ? 'text-white' : 'text-indigo-300 group-hover:text-white'}>Organizations</div>
        </Link>
      </div>
      <div className="mb-4">
        <Link className="group flex items-center py-3" href="/contacts">
          <Icon
            name="users"
            className={`mr-2 w-4 h-4 ${isUrl('contacts') ? 'fill-white' : 'fill-indigo-400 group-hover:fill-white'}`}
          />
          <div className={isUrl('contacts') ? 'text-white' : 'text-indigo-300 group-hover:text-white'}>Contacts</div>
        </Link>
      </div>
      <div className="mb-4">
        <Link className="group flex items-center py-3" href="/reports">
          <Icon
            name="printer"
            className={`mr-2 w-4 h-4 ${isUrl('reports') ? 'fill-white' : 'fill-indigo-400 group-hover:fill-white'}`}
          />
          <div className={isUrl('reports') ? 'text-white' : 'text-indigo-300 group-hover:text-white'}>Reports</div>
        </Link>
      </div>
    </div>
  )
}
