import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import inertia from '@inertiajs/vite';
import tailwindcss from '@tailwindcss/vite';
import fs from 'fs';
import path from 'path';

const root = import.meta.dirname;

// spring-inertia reads .vite/manifest.json from the classpath root, so the
// manifest is also emitted into src/main/resources/.vite (committed) instead
// of relying on a Maven copy step that IDE runs skip.
function emitManifestAtClasspathRoot() {
  const target = path.resolve(root, '../../main/resources/.vite/manifest.json')
  return {
    name: 'emit-manifest-at-classpath-root',
    writeBundle(options, bundle) {
      const manifest = bundle['.vite/manifest.json']
      if (!manifest) return
      fs.mkdirSync(path.dirname(target), { recursive: true })
      fs.writeFileSync(target, JSON.stringify(JSON.parse(manifest.source), null, 2) + '\n')
    },
  }
}

export default defineConfig(({ command }) => ({
    // Expose the project root to the client so SourceLinks can open files in
    // VSCode during development.
    define:
        command === 'serve'
            ? { __PROJECT_ROOT__: JSON.stringify(process.cwd()) }
            : {},
    plugins: [
        vue({
            template: {
                transformAssetUrls: {
                    base: null,
                    includeAbsolute: false,
                },
            },
        }),
        inertia(),
        tailwindcss(),
        emitManifestAtClasspathRoot(),
    ],
    resolve: {
        alias: {
            '@': path.resolve(root, 'resources/js'),
        },
    },
    build: {
        minify: false,
        outDir: path.resolve(root, '../../main/resources/static'),
        // Never empty: favicon.svg is committed inside the outDir and all
        // output names are unhashed, so wiping would only delete good files.
        emptyOutDir: false,
        manifest: '.vite/manifest.json',
        rollupOptions: {
            input: path.resolve(root, 'resources/js/app.ts'),
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
}));