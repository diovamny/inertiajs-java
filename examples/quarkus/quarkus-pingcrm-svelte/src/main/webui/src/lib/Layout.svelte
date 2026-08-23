<script lang="ts">
  import { page } from '$app/state';
  import { onMount } from 'svelte';
  
  const props = $props<{ 
    auth: { user: { id: number; first_name: string; last_name: string; email: string; owner: boolean; account: { id: number; name: string } } | null };
    errors: Record<string, string>;
    flash: { success?: string; error?: string };
  }>();
  
  let sidebarOpen = $state(false);
  let userMenuOpen = $state(false);
  
  const userName = $derived(props.auth?.user ? `${props.auth.user.first_name} ${props.auth.user.last_name}` : '');
  const userInitials = $derived(props.auth?.user ? props.auth.user.first_name.charAt(0) + props.auth.user.last_name.charAt(0) : '');
  
  function logout() {
    const form = document.createElement('form');
    form.method = 'DELETE';
    form.action = '/logout';
    document.body.appendChild(form);
    form.submit();
  }
  
  function toggleSidebar() {
    sidebarOpen = !sidebarOpen;
  }
  
  function closeSidebar() {
    sidebarOpen = false;
  }
  
  function toggleUserMenu() {
    userMenuOpen = !userMenuOpen;
  }
  
  function closeUserMenu() {
    userMenuOpen = false;
  }
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-950">
  <!-- Mobile sidebar backdrop -->
  <div class="fixed inset-0 z-40 bg-black/50 lg:hidden" 
       class:hidden={!sidebarOpen}
       on:click={closeSidebar}
       aria-hidden="true"></div>
  
  <!-- Sidebar -->
  <aside class="fixed inset-y-0 left-0 z-50 w-64 bg-white dark:bg-gray-900 transform transition-transform duration-300 ease-in-out lg:translate-x-0"
       class:translate-x-0={sidebarOpen}
       class:-translate-x-full={!sidebarOpen}
       aria-label="Sidebar">
    <div class="flex h-16 items-center justify-between px-4 border-b border-gray-200 dark:border-gray-700">
      <h1 class="text-xl font-bold text-primary-600">Ping CRM</h1>
      <button class="lg:hidden p-2 rounded-md text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800" 
              on:click={closeSidebar} aria-label="Close sidebar">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
    
    <nav class="mt-4 px-3 space-y-1">
      <a href="/" 
         use:enhance
         class="flex items-center px-3 py-2.5 text-sm font-medium rounded-md text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800">
        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>
        Dashboard
      </a>
      <a href="/contacts" 
         use:enhance
         class="flex items-center px-3 py-2.5 text-sm font-medium rounded-md text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800">
        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        Contacts
      </a>
      <a href="/organizations" 
         use:enhance
         class="flex items-center px-3 py-2.5 text-sm font-medium rounded-md text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800">
        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
        </svg>
        Organizations
      </a>
      <a href="/users" 
         use:enhance
         class="flex items-center px-3 py-2.5 text-sm font-medium rounded-md text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800">
        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9 5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
        Users
      </a>
      <a href="/reports" 
         use:enhance
         class="flex items-center px-3 py-2.5 text-sm font-medium rounded-md text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800">
        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        Reports
      </a>
    </nav>
  </aside>
  
  <!-- Main content -->
  <div class="lg:pl-64 min-h-screen">
    <!-- Top navigation -->
    <header class="sticky top-0 z-30 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
      <div class="flex items-center justify-between h-16 px-4 sm:px-6 lg:px-8">
        <button class="lg:hidden p-2 rounded-md text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800" 
                on:click={toggleSidebar} aria-label="Open sidebar">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        
        <div class="flex-1 lg:flex lg:items-center lg:justify-between">
          <nav class="flex space-x-4 lg:ml-6" aria-label="Breadcrumb">
            {#if $page.url.pathname !== '/'}
              <a href="/" use:enhance class="text-sm text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300">Dashboard</a>
              <svg class="w-4 h-4 text-gray-400 flex items-center" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
              <span class="text-sm text-gray-900 dark:text-white">{$page.url.pathname.split('/').filter(Boolean).pop()?.replace(/-/g, ' ') || 'Dashboard'}</span>
            {/if}
          </nav>
        </div>
        
        <!-- User menu -->
        <div class="relative">
          <button on:click={toggleUserMenu} 
                  class="flex items-center space-x-2 p-2 rounded-md text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800"
                  aria-expanded={userMenuOpen} aria-haspopup="true">
            <div class="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center text-primary-700 dark:text-primary-300 font-medium">
              {userInitials}
            </div>
            <span class="hidden lg:block text-sm font-medium">{userName}</span>
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          
          {#if userMenuOpen}
          <div class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-md shadow-lg border border-gray-200 dark:border-gray-700 py-1 z-50"
               on:click|preventDefault={() => {}} 
               on:mouseleave={closeUserMenu}>
            <div class="px-4 py-2 border-b border-gray-100 dark:border-gray-700">
              <p class="text-sm font-medium text-gray-900 dark:text-white">{userName}</p>
              <p class="text-sm text-gray-500 dark:text-gray-400 truncate">{props.auth?.user?.email}</p>
            </div>
            <a href="/profile" use:enhance class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700">Profile</a>
            <form method="DELETE" action="/logout" on:submit|preventDefault={logout} class="w-full">
              <button type="submit" class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-100 dark:text-red-400 dark:hover:bg-gray-700">Logout</button>
            </form>
          </div>
          {/if}
        </div>
      </header>
      
      <!-- Main content area -->
      <main class="p-4 sm:p-6 lg:p-8">
        <!-- Flash messages -->
        {#if props.flash?.success}
        <div class="mb-4 rounded-md bg-green-50 dark:bg-green-900/30 p-4 border border-green-200 dark:border-green-800 flex items-center justify-between">
          <p class="text-sm text-green-800 dark:text-green-300">{props.flash.success}</p>
          <button on:click={() => props.flash.success = null} class="text-green-500 hover:text-green-700">×</button>
        </div>
        {/if}
        
        {#if props.flash?.error}
        <div class="mb-4 rounded-md bg-red-50 dark:bg-red-900/30 p-4 border border-red-200 dark:border-red-800 flex items-center justify-between">
          <p class="text-sm text-red-800 dark:text-red-300">{props.flash.error}</p>
          <button on:click={() => props.flash.error = null} class="text-red-500 hover:text-red-700">×</button>
        </div>
        {/if}
        
        <!-- Validation errors -->
        {#if Object.keys(props.errors).length > 0}
        <div class="mb-4 rounded-md bg-red-50 dark:bg-red-900/30 p-4 border border-red-200 dark:border-red-800">
          <h3 class="text-sm font-medium text-red-800 dark:text-red-300 mb-2">Please fix the following errors:</h3>
          <ul class="list-disc list-inside text-sm text-red-700 dark:text-red-300 space-y-1">
            {#each Object.entries(props.errors) as [field, message]}
              <li>{field}: {message}</li>
            {/each}
          </ul>
        </div>
        {/if}
        
        <slot />
      </main>
    </div>
  </div>
  
  <script module>
    import { enhance } from '$lib/actions/enhance';
  </script>