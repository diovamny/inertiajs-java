<script>
  import { useForm } from '@inertiajs/svelte'
  import { router } from '@inertiajs/svelte'
  import Layout from '$lib/Layout.svelte'

  let { user } = $props()

  let form = useForm({
    name: user.name,
    email: user.email,
    owner: user.owner,
  })

  let passwordForm = useForm({
    password: '',
    password_confirmation: '',
  })

  function submit(e) {
    e.preventDefault()
    form.put(`/users/${user.id}`)
  }

  function updatePassword(e) {
    e.preventDefault()
    passwordForm.put(`/users/${user.id}/password`)
  }

  function destroy() {
    if (confirm('Are you sure you want to delete this user?')) {
      router.delete(`/users/${user.id}`)
    }
  }

  function restore() {
    if (confirm('Are you sure you want to restore this user?')) {
      router.put(`/users/${user.id}/restore`)
    }
  }
</script>

<Layout>
  <div class="mb-8 flex items-center justify-between">
    <h1 class="text-3xl font-bold">Edit User</h1>
    <div class="flex items-center gap-2">
      {#if user.deleted_at}
        <button onclick={restore} class="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700">Restore</button>
      {:else}
        <button onclick={destroy} class="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700">Delete</button>
      {/if}
    </div>
  </div>

  <div class="mb-6">
    <a href="/users" class="text-indigo-600 hover:text-indigo-800 text-sm font-medium">
      ← Back to Users
    </a>
  </div>

  {#if user.deleted_at}
    <div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-md text-yellow-800">
      This user has been moved to the trash.
    </div>
  {/if}

  <div class="max-w-3xl bg-white rounded-md shadow p-6 mb-8">
    <form onsubmit={submit} class="space-y-6">
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
      </div>

      <div class="flex items-center">
        <input type="checkbox" bind:checked={form.owner} id="owner" class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
        <label for="owner" class="ml-2 text-sm text-gray-600">Owner</label>
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
  </div>

  <div class="max-w-3xl bg-white rounded-md shadow p-6">
    <h2 class="text-lg font-medium mb-4">Update Password</h2>
    <form onsubmit={updatePassword} class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input type="password" bind:value={passwordForm.password} class="w-full" />
          {#if passwordForm.errors.password}
            <p class="mt-1 text-sm text-red-500">{passwordForm.errors.password}</p>
          {/if}
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
          <input type="password" bind:value={passwordForm.password_confirmation} class="w-full" />
          {#if passwordForm.errors.password_confirmation}
            <p class="mt-1 text-sm text-red-500">{passwordForm.errors.password_confirmation}</p>
          {/if}
        </div>
      </div>

      <div class="mt-6">
        <button
          type="submit"
          disabled={passwordForm.processing}
          class="inline-flex items-center px-4 py-2 bg-indigo-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-indigo-700 active:bg-indigo-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition disabled:opacity-50"
        >
          {#if passwordForm.processing}
            Updating...
          {:else}
            Update Password
          {/if}
        </button>
      </div>
    </form>
  </div>
</Layout>