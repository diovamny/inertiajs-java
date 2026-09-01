import './app.css';
import { createInertiaApp } from '@inertiajs/svelte';

createInertiaApp({
  resolve: (name) => {
    const pages = import.meta.glob('./lib/Pages/**/*.svelte', { eager: true });
    const page = pages[`./lib/Pages/${name}.svelte`];
    if (!page) throw new Error(`Page not found: ${name}`);
    return page;
  },
});
