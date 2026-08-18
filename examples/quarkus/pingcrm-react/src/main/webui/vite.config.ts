import { fileURLToPath } from 'url'
import path from 'path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import inertia from '@inertiajs/vite'
import tailwindcss from '@tailwindcss/vite'

const root = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig({
  plugins: [react(), inertia(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(root, 'resources/js'),
    },
  },
  build: {
    outDir: path.resolve(root, '../../main/resources/META-INF/resources'),
    emptyOutDir: true,
    rollupOptions: {
      input: path.resolve(root, 'resources/js/app.tsx'),
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name][extname]',
      },
    },
  },
  server: {
    port: 5173,
    strictPort: false,
  },
})
