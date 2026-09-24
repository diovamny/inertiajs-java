<script>
  import { usePage } from '@inertiajs/svelte'

  let page = usePage()
  let greeting = $derived(page.props?.greeting ?? '')
</script>

<div class="mx-auto max-w-3xl p-8">
  <h1 class="mb-2 text-3xl font-bold">Probe Target</h1>
  <p data-testid="probe-target-greeting" class="mb-6">{greeting}</p>
  <a href="/e2e-probe" class="text-indigo-500 hover:underline">Back to probe</a>
</div>
