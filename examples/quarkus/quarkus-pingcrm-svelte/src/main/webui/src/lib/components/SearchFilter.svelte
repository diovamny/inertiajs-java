<script>
  let { onNavigate } = $props()
  let localSearch = $state('')
  let localTrashed = $state('')

  function handleSearch() {
    if (onNavigate) {
      onNavigate({ search: localSearch, trashed: localTrashed })
    }
  }
</script>

<div class="mb-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
  <div class="flex items-center gap-2">
    <div class="relative">
      <input
        type="text"
        placeholder="Search..."
        bind:value={localSearch}
        onkeydown={(e) => { if (e.key === 'Enter') handleSearch() }}
        class="w-full sm:w-64 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm pl-8"
      />
      <svg class="absolute left-2 top-2.5 h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
      </svg>
    </div>
    <select
      bind:value={localTrashed}
      onchange={handleSearch}
      class="rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
    >
      <option value="">All</option>
      <option value="with">With Trashed</option>
      <option value="only">Only Trashed</option>
    </select>
  </div>
</div>
