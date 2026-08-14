import { createInertiaApp } from '@inertiajs/vue3';
import { config } from '@inertiajs/core';
import '../css/app.css';
import { initializeTheme } from './composables/useAppearance';

const appName = import.meta.env.VITE_APP_NAME || 'Inertia Kitchen Sink';

config.set('form.forceIndicesArrayFormatInFormData', false);

createInertiaApp({
    title: (title) => (title ? `${title} - ${appName}` : appName),
    defaults: {
        visitOptions: (href, options) => ({
            preserveScroll: options?.preserveScroll ?? 'errors',
            ...options,
        }),
    },
});

// This will set light / dark mode on page load...
initializeTheme();
