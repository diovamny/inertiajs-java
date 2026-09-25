import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// spring-inertia reads .vite/manifest.json from the classpath root, so the
// manifest is also emitted into src/main/resources/.vite (same pattern as
// the other demos) instead of relying on hand-placed files that a fresh
// checkout/CI never has.
function emitManifestAtClasspathRoot() {
  const target = path.resolve(__dirname, '../../main/resources/.vite/manifest.json');
  return {
    name: 'emit-manifest-at-classpath-root',
    writeBundle(options, bundle) {
      const manifest = bundle['.vite/manifest.json'];
      if (!manifest) return;
      fs.mkdirSync(path.dirname(target), { recursive: true });
      fs.writeFileSync(target, JSON.stringify(JSON.parse(manifest.source), null, 2) + '\n');
    },
  };
}

export default defineConfig({
  plugins: [svelte(), emitManifestAtClasspathRoot()],
  resolve: {
    alias: {
      '$lib': path.resolve(__dirname, 'src/lib'),
      '$components': path.resolve(__dirname, 'src/lib/Components'),
      '$stores': path.resolve(__dirname, 'src/lib/stores'),
      '$types': path.resolve(__dirname, 'src/lib/types'),
    },
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    manifest: true,
    rollupOptions: {
      input: {
        app: path.resolve(__dirname, 'src/main.ts'),
      },
      output: {
        entryFileNames: 'assets/app.js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/app.[ext]',
      },
    },
  },
});