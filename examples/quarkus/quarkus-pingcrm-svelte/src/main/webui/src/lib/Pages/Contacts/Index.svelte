<script>
  import { router } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'
  import SearchFilter from '$lib/Components/SearchFilter.svelte'
  import Pagination from '$lib/Components/Pagination.svelte'

  let { contacts, filters } = $props()
  let filterSearch = $state(filters?.search || '')
  let filterTrashed = $state(filters?.trashed || '')

  function destroy(id) {
    if (confirm('Are you sure you want to delete this contact?')) {
      router.delete(`/contacts/${id}`)
    }
  }
</script>

<Layout>
  <div class="mb-8 flex items-center justify-between">
    <h1 class="text-3xl font-bold">Contacts</h1>
    <a
      href="/contacts/create"
      class="inline-flex items-center px-4 py-2 bg-indigo-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-indigo-700 active:bg-indigo-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition"
    >
      <svg class="-ml-0.5 mr-1.5 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
      </svg>
      Create
    </a>
  </div>

  <SearchFilter bind:search={filterSearch} bind:trashed={filterTrashed} />

  <div class="bg-white rounded-md shadow overflow-x-auto">
    <table class="w-full whitespace-nowrap text-left">
      <thead class="bg-gray-50 border-b border-gray-200">
        <tr>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Organization</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">City</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Phone</th>
          <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider"></th>
        </tr>
      </thead>
      <tbody>
        {#each contacts.data as contact (contact.id)}
          <tr class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4">
              <a href="/contacts/{contact.id}/edit" class="text-indigo-600 hover:text-indigo-900 font-medium">
                {contact.name}
              </a>
              {#if contact.deleted_at}
                <svg class="inline-block ml-1 h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              {/if}
            </td>
            <td class="px-6 py-4">{contact.organization_id || ''}</td>
            <td class="px-6 py-4">{contact.city || ''}</td>
            <td class="px-6 py-4">{contact.phone || ''}</td>
            <td class="px-6 py-4 text-right">
              <button
                onclick={() => destroy(contact.id)}
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

  <Pagination links={contacts.links} />
</Layout>
