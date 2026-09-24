import type { ReactNode } from 'react'
import { Link, usePage } from '@inertiajs/react'
import Dropdown from '@/Shared/Dropdown'
import FlashMessages from '@/Shared/FlashMessages'
import Icon from '@/Shared/Icon'
import Logo from '@/Shared/Logo'
import MainMenu from '@/Shared/MainMenu'
import type { PageProps } from '@/types'

export default function Layout({ children }: { children: ReactNode }) {
  const { auth } = usePage<PageProps>().props

  return (
    <div>
      <div id="dropdown" />
      <div className="md:flex md:flex-col">
        <div className="md:flex md:flex-col md:h-screen">
          <div className="md:flex md:shrink-0">
            <div className="flex items-center justify-between px-6 py-4 bg-indigo-900 md:shrink-0 md:justify-center md:w-56">
              <Link className="mt-1" href="/">
                <Logo className="fill-white" width={120} height={28} />
              </Link>
              <Dropdown
                className="md:hidden"
                placement="bottom-end"
                trigger={
                  <svg className="w-6 h-6 fill-white" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path d="M0 3h20v2H0V3zm0 6h20v2H0V9zm0 6h20v2H0v-2z" />
                  </svg>
                }
              >
                <div className="mt-2 px-8 py-4 bg-indigo-800 rounded shadow-lg">
                  <MainMenu />
                </div>
              </Dropdown>
            </div>
            <div className="md:text-md flex items-center justify-between p-4 w-full text-sm bg-white border-b md:px-12 md:py-0">
              <div className="mr-4 mt-1">{auth.user?.account.name}</div>
              <Dropdown
                className="mt-1"
                placement="bottom-end"
                trigger={
                  <div className="group flex items-center cursor-pointer select-none">
                    <div className="mr-1 text-gray-700 group-hover:text-indigo-600 focus:text-indigo-600 whitespace-nowrap">
                      <span>{auth.user?.first_name}</span>
                      <span className="hidden md:inline">&nbsp;{auth.user?.last_name}</span>
                    </div>
                    <Icon className="w-5 h-5 fill-gray-700 group-hover:fill-indigo-600 focus:fill-indigo-600" name="cheveron-down" />
                  </div>
                }
              >
                <div className="mt-2 py-2 text-sm bg-white rounded shadow-xl">
                  <Link className="block px-6 py-2 hover:text-white hover:bg-indigo-500" href={`/users/${auth.user?.id}/edit`}>
                    My Profile
                  </Link>
                  <Link className="block px-6 py-2 hover:text-white hover:bg-indigo-500" href="/users">
                    Manage Users
                  </Link>
                  <Link className="block px-6 py-2 w-full text-left hover:text-white hover:bg-indigo-500" href="/logout" method="delete" as="button">
                    Logout
                  </Link>
                </div>
              </Dropdown>
            </div>
          </div>
          <div className="md:flex md:grow md:overflow-hidden">
            <div className="hidden shrink-0 p-12 w-56 bg-indigo-800 overflow-y-auto md:block">
              <MainMenu />
            </div>
            <div className="px-4 py-8 md:flex-1 md:p-12 md:overflow-y-auto">
              <FlashMessages />
              {children}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
