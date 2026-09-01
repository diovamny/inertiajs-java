<script>
  import { useForm, router } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'

  let { user } = $props()

  let form = useForm({
    first_name: user.first_name || '',
    last_name: user.last_name || '',
    email: user.email || '',
    password: '',
    owner: user.owner || false,
    photo: null,
  })

  let photoPreview = $state(null)

  function update(e) {
    e.preventDefault()
    form.put(`/users/${user.id}`)
  }

  function destroy() {
    if (confirm('Are you sure you want to delete this user?')) {
      router.delete(`/users/${user.id}`)
    }
  }

  function restore() {
    router.put(`/users/${user.id}/restore`)
  }

  function handlePhoto(e) {
    form.photo = e.target.files[0]
    if (e.target.files[0]) {
      photoPreview = URL.createObjectURL(e.target.files[0])
    }
  }
</script>

<Layout>
  <div class="mb-8 flex items-center justify-between">
    <h1 class="text-3xl font-bold">
      <a href="/users" class="text-indigo-600 hover:text-indigo-800">Users</a>
      <span class="mx-2 text-gray-400">/</span>
      {user.first_name} {user.last_name}
    </h1>
    <div class="flex items-center gap-2">
      {#if user.deleted_at}
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

  {#if user.deleted_at}
    <div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-md text-yellow-800 text-sm">
      This user has been deleted.
    </div>
  {/if}

  <form onsubmit={update} class="max-w-3xl bg-white rounded-md shadow p-6" enctype="multipart/form-data">
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
        <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
        <input type="email" bind:value={form.email} class="w-full" />
        {#if form.errors.email}
          <p class="mt-1 text-sm text-red-500">{form.errors.email}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
        <input type="password" bind:value={form.password} class="w-full" placeholder="(unchanged)" />
        {#if form.errors.password}
          <p class="mt-1 text-sm text-red-500">{form.errors.password}</p>
        {/if}
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Photo</label>
        <div class="flex items-center gap-4">
          {#if photoPreview}
            <img src={photoPreview} class="w-12 h-12 rounded-full" alt="" />
          {:else if user.photo_path}
            <img src="/img/{user.photo_path}?w=60&h=60&fit=crop" class="w-12 h-12 rounded-full" alt="" />
          {/if}
          <input type="file" onchange={handlePhoto} class="text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100" />
        </div>
        {#if form.errors.photo}
          <p class="mt-1 text-sm text-red-500">{form.errors.photo}</p>
        {/if}
      </div>

      <div class="flex items-center gap-2">
        <input
          type="checkbox"
          bind:checked={form.owner}
          id="owner"
          class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
        />
        <label for="owner" class="text-sm text-gray-700">Owner</label>
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
          Update User
        {/if}
      </button>
      <a href="/users" class="text-sm text-gray-600 hover:text-gray-900">Cancel</a>
    </div>
  </form>
</Layout>
