<script>
  import { router } from '@inertiajs/svelte'
  import FlashMessages from './FlashMessages.svelte'
  import Logo from './Logo.svelte'
  import MainMenu from './MainMenu.svelte'

  let { children } = $props()
  let dropdownOpen = $state(false)
  let mobileOpen = $state(false)

  function toggleDropdown() {
    dropdownOpen = !dropdownOpen
  }

  function toggleMobile() {
    mobileOpen = !mobileOpen
  }

  function logout() {
    router.delete('/logout')
  }
</script>

<div class="min-h-screen bg-gray-100">
  <div class="md:hidden fixed top-0 left-0 right-0 z-50 bg-indigo-900 h-14 flex items-center px-4">
    <button onclick={toggleMobile} class="text-white p-1" aria-label="Menu">
      <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
      </svg>
    </button>
    <div class="ml-4">
      <Logo />
    </div>
  </div>

  {#if mobileOpen}
    <div class="fixed inset-0 z-40 md:hidden">
      <!-- svelte-ignore a11y_click_events_have_key_events -->
      <!-- svelte-ignore a11y_no_static_element_interactions -->
      <div class="fixed inset-0 bg-black/50" onclick={toggleMobile}></div>
      <div class="fixed inset-y-0 left-0 w-64 bg-indigo-900 z-50 overflow-y-auto">
        <div class="p-4">
          <Logo />
          <div class="mt-4">
            <MainMenu />
          </div>
        </div>
      </div>
    </div>
  {/if}

  <div class="flex">
    <div class="hidden md:block fixed inset-y-0 left-0 w-64 bg-indigo-900 z-30 overflow-y-auto">
      <div class="p-4 pt-6">
        <Logo />
        <div class="mt-8">
          <MainMenu />
        </div>
      </div>
    </div>

    <div class="flex-1 md:ml-64">
      <div class="hidden md:flex h-14 bg-white border-b items-center justify-between px-6">
        <div></div>
        <div class="relative">
          <button
            onclick={toggleDropdown}
            class="flex items-center text-sm text-gray-700 hover:text-gray-900 focus:outline-none"
          >
            <span class="mr-1">John</span>
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
            </svg>
          </button>
          {#if dropdownOpen}
            <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50">
              <a href="/contacts" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100" onclick={() => { dropdownOpen = false }}>My Contacts</a>
              <hr class="my-1" />
              <button onclick={logout} class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Logout</button>
            </div>
          {/if}
        </div>
      </div>

      <div class="md:hidden h-14"></div>

      <FlashMessages />

      <div class="p-6">
        {@render children()}
      </div>
    </div>
  </div>
</div>