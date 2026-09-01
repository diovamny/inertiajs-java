<script>
  import { useForm } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'

  let form = useForm({
    name: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    region: '',
    country: '',
    postal_code: '',
  })

  function submit(e) {
    e.preventDefault()
    form.post('/organizations')
  }
</script>

<Layout>
  <div class="mb-8">
    <h1 class="text-3xl font-bold">Create Organization</h1>
  </div>

  <div class="mb-6">
    <a href="/organizations" class="text-indigo-600 hover:text-indigo-800 text-sm font-medium">
      ← Back to Organizations
    </a>
  </div>

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
          Creating...
        {:else}
          Create Organization
        {/if}
      </button>
      <a href="/organizations" class="text-sm text-gray-600 hover:text-gray-900">Cancel</a>
    </div>
  </form>
</Layout>