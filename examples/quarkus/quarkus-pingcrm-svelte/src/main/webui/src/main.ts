import './app.css';
import { createInertiaApp } from '@inertiajs/svelte';
import Layout from './lib/Layout.svelte';

createInertiaApp({
  resolve: (name) => {
    const pages = import.meta.glob('./lib/Pages/**/*.svelte', { eager: true });
    const page = pages[`./lib/Pages/${name}.svelte`];
    if (!page) throw new Error(`Page not found: ${name}`);
    return page;
  },
  setup({ el, App, props }) {
    new App({ target: el, props });
  },
  title: (title) => `${title} - Ping CRM`,
});