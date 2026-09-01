<script>
  import { router } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'
  import SearchFilter from '$lib/Components/SearchFilter.svelte'

  let { users, filters } = $props()
  let filterSearch = $state(filters?.search || '')
  let filterRole = $state(filters?.role || '')
  let filterTrashed = $state(filters?.trashed || '')

  function destroy(id) {
    if (confirm('Are you sure you want to delete this user?')) {
      router.delete(`/users/${id}`)
    }
  }
</script>

<Layout>
  <div class="mb-8 flex items-center justify-between">
    <h1 class="text-3xl font-bold">Users</h1>
    <a
      href="/users/create"
      class="inline-flex items-center px-4 py-2 bg-indigo-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-indigo-700 active:bg-indigo-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition"
    >
      <svg class="-ml-0.5 mr-1.5 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
      </svg>
      Create
    </a>
  </div>

  <div class="mb-6 flex items-center gap-2">
    <div class="relative">
      <input
        type="text"
        placeholder="Search..."
        bind:value={filterSearch}
        onkeydown={(e) => { if (e.key === 'Enter') window.location.href = `/users?search=${filterSearch}&role=${filterRole}&trashed=${filterTrashed}` }}
        class="w-full sm:w-64 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm pl-8"
      />
      <svg class="absolute left-2 top-2.5 h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
      </svg>
    </div>
    <select
      bind:value={filterRole}
      onchange={() => window.location.href = `/users?search=${filterSearch}&role=${filterRole}&trashed=${filterTrashed}`}
      class="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
    >
      <option value="">All Roles</option>
      <option value="user">User</option>
      <option value="owner">Owner</option>
    </select>
    <select
      bind:value={filterTrashed}
      onchange={() => window.location.href = `/users?search=${filterSearch}&role=${filterRole}&trashed=${filterTrashed}`}
      class="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
    >
      <option value="">All</option>
      <option value="with">With Trashed</option>
      <option value="only">Only Trashed</option>
    </select>
  </div>

  <div class="bg-white rounded-md shadow overflow-x-auto">
    <table class="w-full whitespace-nowrap text-left">
      <thead class="bg-gray-50 border-b border-gray-200">
        <tr>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider"></th>
        </tr>
      </thead>
      <tbody>
        {#each users as user (user.id)}
          <tr class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4">
              <div class="flex items-center">
                {#if user.photo}
                  <img src={user.photo} class="w-8 h-8 rounded-full mr-3" alt="" />
                {:else}
                  <div class="w-8 h-8 rounded-full bg-indigo-200 text-indigo-600 flex items-center justify-center font-bold text-xs mr-3">
                    {user.name?.charAt(0) || '?'}
                  </div>
                {/if}
                <a href="/users/{user.id}/edit" class="text-indigo-600 hover:text-indigo-900 font-medium">
                  {user.name}
                </a>
                {#if user.deleted_at}
                  <svg class="inline-block ml-1 h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                  </svg>
                {/if}
              </div>
            </td>
            <td class="px-6 py-4">{user.email}</td>
            <td class="px-6 py-4">{user.owner ? 'Owner' : 'User'}</td>
            <td class="px-6 py-4 text-right">
              <button
                onclick={() => destroy(user.id)}
                class="text-red-600 hover:text-red-900 text-sm"
              >
                Delete
              </button>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
</Layout>