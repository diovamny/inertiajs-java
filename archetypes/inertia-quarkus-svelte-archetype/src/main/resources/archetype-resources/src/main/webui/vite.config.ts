import path from 'path'
import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

const root = import.meta.dirname

export default defineConfig({
  plugins: [svelte()],
  resolve: {
    alias: {
      '@': path.resolve(root, 'src'),
    },
  },
  build: {
    // Quarkus serves classpath resources from META-INF/resources (not static/)
    outDir: path.resolve(root, '../../main/resources/META-INF/resources'),
    // Never empty: brand SVGs are committed inside the outDir and names are unhashed.
    emptyOutDir: false,
    manifest: '.vite/manifest.json',
    rollupOptions: {
      input: path.resolve(root, 'src/app.ts'),
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name][extname]',
      },
    },
  },
  server: {
    port: 5173,
  },
})
