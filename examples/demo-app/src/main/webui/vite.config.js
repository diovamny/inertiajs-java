import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import inertia from '@inertiajs/vite'
import path from 'path'

export default defineConfig({
  plugins: [vue(), inertia()],
  build: {
    outDir: path.resolve(__dirname, '../../main/resources/META-INF/resources'),
    emptyOutDir: true,
    rollupOptions: {
      input: path.resolve(__dirname, 'app.js'),
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
