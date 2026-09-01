<script>
  import { router } from '@inertiajs/svelte'
  let { links } = $props()

  function visit(url) {
    if (url) {
      router.get(url)
    }
  }
</script>

{#if links}
  <div class="flex items-center justify-between mt-6">
    <div class="text-sm text-gray-700">
      Showing {links.from || 0} to {links.to || 0} of {links.total || 0} results
    </div>
    <div class="flex items-center gap-1">
      {#if links.prev}
        <button
          onclick={() => visit(links.prev)}
          class="px-3 py-1 text-sm border rounded hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          Previous
        </button>
      {/if}
      {#if links.window}
        {#each links.window as pageNum}
          <button
            onclick={() => visit(`/contacts?page=${pageNum}`)}
            class="px-3 py-1 text-sm border rounded
                   {links.currentPage === pageNum ? 'bg-indigo-600 text-white border-indigo-600' : 'hover:bg-gray-50'}"
          >
            {pageNum}
          </button>
        {/each}
      {/if}
      {#if links.next}
        <button
          onclick={() => visit(links.next)}
          class="px-3 py-1 text-sm border rounded hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          Next
        </button>
      {/if}
    </div>
  </div>
{/if}