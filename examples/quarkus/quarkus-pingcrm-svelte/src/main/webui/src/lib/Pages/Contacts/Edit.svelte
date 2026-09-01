<script>
  import { useForm, router } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'

  let { contact, organizations = [] } = $props()

  let form = useForm({
    first_name: contact.first_name || '',
    last_name: contact.last_name || '',
    organization_id: contact.organization_id || '',
    email: contact.email || '',
    phone: contact.phone || '',
    address: contact.address || '',
    city: contact.city || '',
    region: contact.region || '',
    country: contact.country || '',
    postal_code: contact.postal_code || '',
  })

  function update(e) {
    e.preventDefault()
    form.put(`/contacts/${contact.id}`)
  }

  function destroy() {
    if (confirm('Are you sure you want to delete this contact?')) {
      router.delete(`/contacts/${contact.id}`)
    }
  }

  function restore() {
    router.put(`/contacts/${contact.id}/restore`)
  }
</script>

<Layout>
  <div class="mb-8 flex items-center justify-between">
    <h1 class="text-3xl font-bold">
      <a href="/contacts" class="text-indigo-600 hover:text-indigo-800">Contacts</a>
      <span class="mx-2 text-gray-400">/</span>
      {contact.first_name} {contact.last_name}
    </h1>
    <div class="flex items-center gap-2">
      {#if contact.deleted_at}
        <button
          onclick={restore}
          class="inline-flex items-center px-4 py-2 bg-green-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-green-700 transition"
        >
          Restore
        </button>
      {:else}
        <button
          onclick={destroy}
          class="inline-flex items-center px-4 py-2 bg-red-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-red-700 transition"
        >
          Delete
        </button>
      {/if}
    </div>
  </div>

  {#if contact.deleted_at}
    <div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-md text-yellow-800 text-sm">
      This contact has been deleted.
    </div>
  {/if}

  <form onsubmit={update} class="max-w-3xl bg-white rounded-md shadow p-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">First Name</label>
        <input type="text" bind:value={form.first_name} class="w-full" />
        {#if form.errors.first_name}
          <p class="mt-1 text-sm text-red-500">{form.errors.first_name}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
        <input type="text" bind:value={form.last_name} class="w-full" />
        {#if form.errors.last_name}
          <p class="mt-1 text-sm text-red-500">{form.errors.last_name}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Organization</label>
        <select bind:value={form.organization_id} class="w-full">
          <option value="">Select...</option>
          {#each organizations as org}
            <option value={org.id}>{org.name}</option>
          {/each}
        </select>
        {#if form.errors.organization_id}
          <p class="mt-1 text-sm text-red-500">{form.errors.organization_id}</p>
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
          Update Contact
        {/if}
      </button>
      <a href="/contacts" class="text-sm text-gray-600 hover:text-gray-900">Cancel</a>
    </div>
  </form>
</Layout>
