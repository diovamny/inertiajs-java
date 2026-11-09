import path from 'path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const root = import.meta.dirname

export default defineConfig(({ isSsrBuild }) => ({
  plugins: [
    vue({
      template: {
        // '/brand/*.svg' is served by Quarkus from the classpath, not bundled
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
    // Quarkus serves classpath resources from META-INF/resources (not static/)
    outDir: path.resolve(root, '../../main/resources/META-INF/resources'),
    // Never empty: brand SVGs are committed inside the outDir and names are unhashed.
    emptyOutDir: false,
    manifest: '.vite/manifest.json',
    rollupOptions: {
      input: path.resolve(root, 'src/app.ts'),
      output: {
        entryFileNames: isSsrBuild ? '[name].js' : 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name][extname]',
      },
    },
  },
  server: {
    port: 5173,
    // Proxy API calls to the backend during `npm run dev` when needed:
    // proxy: { '/api': 'http://localhost:8080' },
  },
}))
