import path from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const root = import.meta.dirname

export default defineConfig({
  plugins: [
    vue({
      template: {
        // '/brand/*.svg' is served by Spring from the classpath, not bundled
        transformAssetUrls: {
          base: null,
          includeAbsolute: false,
        },
      },
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(root, 'src'),
    },
  },
  build: {
    outDir: path.resolve(root, '../../main/resources/static'),
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
