<script>
  import { useForm } from '@inertiajs/svelte'
  import { router } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'

  let { organization, contacts = { data: [], links: null } } = $props()

  let form = useForm({
    name: organization.name,
    email: organization.email,
    phone: organization.phone,
    address: organization.address,
    city: organization.city,
    region: organization.region,
    country: organization.country,
    postal_code: organization.postal_code,
  })

  function submit(e) {
    e.preventDefault()
    form.put(`/organizations/${organization.id}`)
  }

  function destroy() {
    if (confirm('Are you sure you want to delete this organization?')) {
      router.delete(`/organizations/${organization.id}`)
    }
  }

  function restore() {
    if (confirm('Are you sure you want to restore this organization?')) {
      router.put(`/organizations/${organization.id}/restore`)
    }
  }
</script>

<Layout>
  <div class="mb-8 flex items-center justify-between">
    <h1 class="text-3xl font-bold">Edit Organization</h1>
    <div class="flex items-center gap-2">
      {#if organization.deleted_at}
        <button onclick={restore} class="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700">Restore</button>
      {:else}
        <button onclick={destroy} class="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700">Delete</button>
      {/if}
    </div>
  </div>

  <div class="mb-6">
    <a href="/organizations" class="text-indigo-600 hover:text-indigo-800 text-sm font-medium">
      ← Back to Organizations
    </a>
  </div>

  {#if organization.deleted_at}
    <div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-md text-yellow-800">
      This organization has been moved to the trash.
    </div>
  {/if}

  <form onsubmit={submit} class="max-w-3xl bg-white rounded-md shadow p-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
        <input type="text" bind:value={form.name} class="w-full" />
        {#if form.errors.name}
          <p class="mt-1 text-sm text-red-500">{form.errors.name}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
        <input type="email" bind:value={form.email} class="w-full" />
        {#if form.errors.email}
          <p class="mt-1 text-sm text-red-500">{form.errors.email}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
        <input type="text" bind:value={form.phone} class="w-full" />
        {#if form.errors.phone}
          <p class="mt-1 text-sm text-red-500">{form.errors.phone}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
        <input type="text" bind:value={form.address} class="w-full" />
        {#if form.errors.address}
          <p class="mt-1 text-sm text-red-500">{form.errors.address}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">City</label>
        <input type="text" bind:value={form.city} class="w-full" />
        {#if form.errors.city}
          <p class="mt-1 text-sm text-red-500">{form.errors.city}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Province/State</label>
        <input type="text" bind:value={form.region} class="w-full" />
        {#if form.errors.region}
          <p class="mt-1 text-sm text-red-500">{form.errors.region}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Country</label>
        <input type="text" bind:value={form.country} class="w-full" />
        {#if form.errors.country}
          <p class="mt-1 text-sm text-red-500">{form.errors.country}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Postal Code</label>
        <input type="text" bind:value={form.postal_code} class="w-full" />
        {#if form.errors.postal_code}
          <p class="mt-1 text-sm text-red-500">{form.errors.postal_code}</p>
        {/if}
      </div>
    </div>

    <div class="mt-6 flex items-center gap-4">
      <button
        type="submit"
        disabled={form.processing}
        class="inline-flex items-center px-4 py-2 bg-indigo-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-indigo-700 active:bg-indigo-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition disabled:opacity-50"
      >
        {#if form.processing}
          Updating...
        {:else}
          Update Organization
        {/if}
      </button>
      <a href="/organizations" class="text-sm text-gray-600 hover:text-gray-900">Cancel</a>
    </div>
  </form>

  {#if contacts.data.length > 0}
    <div class="mt-10">
      <h2 class="text-xl font-semibold mb-4">Contacts</h2>
      <div class="bg-white rounded-md shadow overflow-x-auto">
        <table class="w-full whitespace-nowrap text-left">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
              <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
              <th class="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Phone</th>
            </tr>
          </thead>
          <tbody>
            {#each contacts.data as c (c.id)}
              <tr class="border-b border-gray-100 hover:bg-gray-50">
                <td class="px-6 py-4">
                  <a href="/contacts/{c.id}/edit" class="text-indigo-600 hover:text-indigo-900 font-medium">
                    {c.name}
                  </a>
                </td>
                <td class="px-6 py-4">{c.email}</td>
                <td class="px-6 py-4">{c.phone || ''}</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  {/if}
</Layout>