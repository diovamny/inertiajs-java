<script lang="ts">
  import Layout from '../../Layout.svelte';
  
  const props = $props<{ errors: Record<string, string> }>();
  
  let email = $state('');
  let password = $state('');
  let loading = $state(false);
  
  function handleSubmit(event: Event) {
    event.preventDefault();
    loading = true;
    
    // Inertia will handle the form submission via enhance
  }
</script>

<Layout {props}>
  <div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
          Sign in to your account
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
          Or <a href="/register" class="font-medium text-primary-600 hover:text-primary-500">create a new account</a>
        </p>
      </div>
      
      <form class="mt-8 space-y-6" onsubmit={handleSubmit} use:enhance>
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="email" class="sr-only">Email address</label>
            <input id="email" name="email" type="email" autocomplete="email" required
                   bind:value={email}
                   class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-primary-500 focus:border-primary-500 focus:z-10 sm:text-sm dark:bg-gray-800 dark:border-gray-600 dark:text-white"
                   placeholder="Email address" />
            {#if props.errors?.email}
              <p class="mt-1 text-sm text-red-600 dark:text-red-400">{props.errors.email}</p>
            {/if}
          </div>
          <div>
            <label for="password" class="sr-only">Password</label>
            <input id="password" name="password" type="password" autocomplete="current-password" required
                   bind:value={password}
                   class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-primary-500 focus:border-primary-500 focus:z-10 sm:text-sm dark:bg-gray-800 dark:border-gray-600 dark:text-white"
                   placeholder="Password" />
            {#if props.errors?.password}
              <p class="mt-1 text-sm text-red-600 dark:text-red-400">{props.errors.password}</p>
            {/if}
          </div>
        </div>
        
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <input id="remember_me" name="remember" type="checkbox" class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded" />
            <label for="remember_me" class="ml-2 block text-sm text-gray-900 dark:text-gray-300">Remember me</label>
          </div>
          
          <div class="text-sm">
            <a href="/forgot-password" class="font-medium text-primary-600 hover:text-primary-500">Forgot your password?</a>
          </div>
        </div>
        
        <div>
          <button type="submit" disabled={loading} class="w-full btn-primary py-2 px-4">
            {#if loading}
              <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
              </svg>
              Signing in...
            {:else}
              Sign in
            {/if}
          </button>
        </div>
      </form>
    </div>
  </div>
</Layout>