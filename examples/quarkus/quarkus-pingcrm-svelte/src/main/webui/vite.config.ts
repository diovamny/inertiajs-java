import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [svelte()],
  resolve: {
    alias: {
      $lib: path.resolve(__dirname, 'src/lib'),
      $components: path.resolve(__dirname, 'src/lib/components'),
      $stores: path.resolve(__dirname, 'src/lib/stores'),
      $types: path.resolve(__dirname, 'src/lib/types'),
      '$app/state': path.resolve(__dirname, 'src/lib/stubs/app-state.ts'),
    },
  },
  build: {
    outDir: '../resources/META-INF/resources',
    emptyOutDir: true,
    manifest: true,
    rollupOptions: {
      input: {
        main: path.resolve(__dirname, 'src/main.ts'),
      },
      external: ['$app/state'],
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8082',
    },
  },
});