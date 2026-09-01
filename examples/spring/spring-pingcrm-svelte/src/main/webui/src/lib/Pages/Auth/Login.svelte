<script>
  import { useForm } from '@inertiajs/svelte'

  let form = useForm({
    email: 'johndoe@example.com',
    password: 'secret',
    remember: true,
  })

  function submit(e) {
    e.preventDefault()
    form.post('/login')
  }
</script>

<div class="flex min-h-screen items-center justify-center bg-indigo-500 p-6">
  <div class="w-full max-w-md">
    <div class="text-center mb-6">
      <svg class="mx-auto h-16 w-16 text-white" viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="30" cy="30" r="28" stroke="currentColor" stroke-width="3" fill="none"/>
        <text x="30" y="38" text-anchor="middle" fill="currentColor" font-size="24" font-weight="bold">P</text>
      </svg>
      <h1 class="mt-4 text-3xl font-bold text-white">Ping CRM</h1>
      <p class="mt-1 text-indigo-200">Welcome Back!</p>
    </div>

    <form onsubmit={submit} class="bg-white rounded-lg shadow-md p-8">
      <div class="mb-4">
        <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
        <input
          id="email"
          type="email"
          bind:value={form.email}
          class="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
        {#if form.errors.email}
          <p class="mt-1 text-sm text-red-500">{form.errors.email}</p>
        {/if}
      </div>

      <div class="mb-4">
        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
        <input
          id="password"
          type="password"
          bind:value={form.password}
          class="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
        {#if form.errors.password}
          <p class="mt-1 text-sm text-red-500">{form.errors.password}</p>
        {/if}
      </div>

      <div class="mb-4">
        <label class="flex items-center">
          <input type="checkbox" bind:checked={form.remember} class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
          <span class="ml-2 text-sm text-gray-600">Remember me</span>
        </label>
      </div>

      <button
        type="submit"
        disabled={form.processing}
        class="w-full rounded-md bg-indigo-600 px-4 py-2 text-white font-semibold hover:bg-indigo-700 disabled:opacity-50 transition"
      >
        {#if form.processing}
          Logging in...
        {:else}
          Login
        {/if}
      </button>
    </form>
  </div>
</div>