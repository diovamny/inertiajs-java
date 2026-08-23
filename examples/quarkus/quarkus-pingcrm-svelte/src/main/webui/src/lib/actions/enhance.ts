import type { Action } from 'svelte/action';

interface EnhanceOptions {
  /** Custom event handler for form submission */
  onsubmit?: (event: SubmitEvent) => void;
  /** Custom event handler for form validation errors */
  onerror?: (errors: Record<string, string>) => void;
  /** Custom event handler for successful submission */
  onsuccess?: (event: SubmitEvent) => void;
}

/**
 * Enhance action for Inertia.js forms in Svelte 5
 * This is a Svelte 5 compatible implementation of the Inertia enhance action
 */
export function enhance(
  form: HTMLFormElement,
  options: EnhanceOptions = {}
): ActionReturn<EnhanceOptions> {
  async function handleSubmit(event: SubmitEvent) {
    event.preventDefault();
    
    const formData = new FormData(form);
    const url = form.action || window.location.href;
    const method = form.method || 'POST';
    
    try {
      const response = await fetch(url, {
        method,
        headers: {
          'X-Inertia': 'true',
          'X-Requested-With': 'XMLHttpRequest',
        },
        body: formData,
        credentials: 'same-origin',
      });
      
      // Handle redirects
      if (response.redirected) {
        window.location.href = response.url;
        return;
      }
      
      // Handle Inertia responses
      const contentType = response.headers.get('Content-Type');
      
      if (contentType?.includes('application/json')) {
        const data = await response.json();
        
        // Handle validation errors (422)
        if (response.status === 422 && data.errors) {
          if (options.onerror) {
            options.onerror(data.errors);
          }
          // Dispatch a custom event for error handling
          form.dispatchEvent(new CustomEvent('inertia:error', { detail: data.errors }));
          return;
        }
        
        // Handle successful form submission
        if (options.onsuccess) {
          options.onsuccess(event);
        }
        
        // Handle Inertia visits (redirects via X-Inertia-Location)
        const location = response.headers.get('X-Inertia-Location');
        if (location) {
          window.location.href = location;
          return;
        }
        
        // If it's a full page reload (Inertia visit)
        if (response.status === 200 && data.component) {
          // This is an Inertia page response - we need to do a full page reload
          // or use the Inertia router to navigate
          window.location.reload();
          return;
        }
      }
      
      // Fallback: reload the page
      window.location.reload();
    } catch (error) {
      console.error('Inertia enhance error:', error);
      window.location.reload();
    }
  }
  
  form.addEventListener('submit', handleSubmit);
  
  return {
    update(newOptions: EnhanceOptions) {
      Object.assign(options, newOptions);
    },
    destroy() {
      form.removeEventListener('submit', handleSubmit);
    },
  };
}

interface ActionReturn<P> {
  update?(params: P): void;
  destroy?(): void;
}