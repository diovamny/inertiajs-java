import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import inertia from '@inertiajs/vite'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

const root = import.meta.dirname

export default defineConfig({
  plugins: [vue(), inertia(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(root, 'resources/js'),
    },
  },
  build: {
    outDir: path.resolve(root, '../../main/resources/META-INF/resources'),
    emptyOutDir: true,
    rollupOptions: {
      input: path.resolve(root, 'resources/js/app.js'),
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name][extname]'
      }
    }
  },
  server: {
    port: 5173,
    strictPort: false
  }
})
