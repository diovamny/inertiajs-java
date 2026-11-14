import path from 'path'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const root = import.meta.dirname

export default defineConfig(({ isSsrBuild }) => ({
  plugins: [react()],
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
      input: path.resolve(root, 'src/app.tsx'),
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
