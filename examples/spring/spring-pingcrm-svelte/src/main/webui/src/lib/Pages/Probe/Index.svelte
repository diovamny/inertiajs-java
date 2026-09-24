<script>
  import { router, useForm, usePage } from '@inertiajs/svelte'

  let page = usePage()

  let form = useForm({ name: '', email: '' })
  let secondary = useForm({ title: '', body: '' })
  let upload = useForm({ photo: null })

  let greeting = $derived(page.props?.greeting ?? '')
  let notice = $derived(page.props?.notice ?? null)
  let slow = $derived(page.props?.slow ?? null)
  let entries = $derived(page.props?.entries ?? [])
  let success = $derived(page.props?.success ?? null)
  let bagErrors = $derived(page.props?.errors?.probeSecondary ?? null)

  function submitPrimary(e) {
    e.preventDefault()
    form.post('/e2e-probe/validate')
  }

  function submitSecondary(e) {
    e.preventDefault()
    secondary.post('/e2e-probe/validate-secondary', {
      headers: { 'X-Inertia-Error-Bag': 'probeSecondary' },
    })
  }

  function submitUpload(e) {
    e.preventDefault()
    upload.post('/e2e-probe/upload')
  }

  function loadSlow() {
    router.reload({ only: ['slow'] })
  }

  function suppressNotice() {
    router.reload({ headers: { 'X-Inertia-Except-Once-Props': 'notice' } })
  }

  function fetchNextEntry() {
    router.reload({ only: ['entries'] })
  }

  function visitTarget() {
    router.visit('/e2e-probe/target?delay=2', {
      component: 'Probe/Target',
      pageProps: (current, shared) => ({
        ...shared,
        greeting: `Navigating from probe (was: "${current.greeting}")`,
      }),
    })
  }

  function redirectBack() {
    router.post('/e2e-probe/redirect-back')
  }

  function onPhoto(e) {
    upload.photo = e.target.files?.[0] ?? null
  }
</script>

<div class="mx-auto max-w-3xl p-8">
  <h1 class="mb-2 text-3xl font-bold">E2E Probe</h1>
  <p data-testid="probe-greeting" class="mb-6 text-gray-600">{greeting}</p>

  {#if success}
    <p data-testid="probe-flash" class="mb-6 rounded bg-green-500 p-3 text-white">{success}</p>
  {/if}

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Validation</h2>
    <form onsubmit={submitPrimary}>
      <label for="probe-name" class="mb-1 block text-sm font-medium">Name</label>
      <input id="probe-name" type="text" bind:value={form.name} class="mb-1 w-full rounded border p-2" />
      {#if form.errors.name}
        <p class="mb-2 text-sm text-red-500">{form.errors.name}</p>
      {/if}
      <label for="probe-email" class="mb-1 block text-sm font-medium">Email</label>
      <input id="probe-email" type="text" bind:value={form.email} class="mb-1 w-full rounded border p-2" />
      {#if form.errors.email}
        <p class="mb-2 text-sm text-red-500">{form.errors.email}</p>
      {/if}
      <button type="submit" class="mt-2 rounded bg-indigo-600 px-4 py-2 text-white">Submit probe form</button>
    </form>
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Named error bag</h2>
    <form onsubmit={submitSecondary}>
      <label for="probe-title" class="mb-1 block text-sm font-medium">Title</label>
      <input id="probe-title" type="text" bind:value={secondary.title} class="mb-1 w-full rounded border p-2" />
      <label for="probe-body" class="mb-1 block text-sm font-medium">Body</label>
      <input id="probe-body" type="text" bind:value={secondary.body} class="mb-1 w-full rounded border p-2" />
      <button type="submit" class="mt-2 rounded bg-indigo-600 px-4 py-2 text-white">Submit probe secondary</button>
    </form>
    {#if bagErrors}
      <div data-testid="probe-bag-errors" class="mt-3">
        <h3 class="font-medium">probeSecondary.errors</h3>
        {#each Object.entries(bagErrors) as [key, message]}
          <p class="text-sm text-red-500">{message}</p>
        {/each}
      </div>
    {/if}
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Deferred prop</h2>
    <p data-testid="probe-slow" class="mb-2">{slow ?? 'pending'}</p>
    <button type="button" class="rounded bg-indigo-600 px-4 py-2 text-white" onclick={loadSlow}>
      Load slow prop
    </button>
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Once prop</h2>
    {#if notice}
      <p data-testid="probe-notice" class="mb-2">{notice}</p>
    {/if}
    <button type="button" class="rounded bg-indigo-600 px-4 py-2 text-white" onclick={suppressNotice}>
      Suppress notice
    </button>
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Mergeable entries</h2>
    <p data-testid="probe-entries-count" class="mb-2">{entries.length} entries</p>
    <ul data-testid="probe-entries" class="mb-2">
      {#each entries as entry (entry.id)}
        <li>{entry.id}: {entry.name}</li>
      {/each}
    </ul>
    <button type="button" class="rounded bg-indigo-600 px-4 py-2 text-white" onclick={fetchNextEntry}>
      Fetch next entry
    </button>
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Instant visit</h2>
    <button type="button" class="rounded bg-indigo-600 px-4 py-2 text-white" onclick={visitTarget}>
      Visit target instantly
    </button>
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">Redirect back</h2>
    <button type="button" class="rounded bg-indigo-600 px-4 py-2 text-white" onclick={redirectBack}>
      Submit and redirect back
    </button>
  </section>

  <section class="mb-8 rounded bg-white p-6 shadow">
    <h2 class="mb-4 text-xl font-bold">File upload</h2>
    <form onsubmit={submitUpload}>
      <input id="probe-photo" type="file" class="mb-2 block" onchange={onPhoto} />
      {#if upload.errors.photo}
        <p class="mb-2 text-sm text-red-500">{upload.errors.photo}</p>
      {/if}
      <button type="submit" class="rounded bg-indigo-600 px-4 py-2 text-white">Upload photo</button>
    </form>
  </section>
</div>
