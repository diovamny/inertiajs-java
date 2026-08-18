import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import inertia from '@inertiajs/vite';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

const root = import.meta.dirname;

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
    ],
    resolve: {
        alias: {
            '@': path.resolve(root, 'resources/js'),
        },
    },
    build: {
        minify: false,
        outDir: path.resolve(root, '../../main/resources/META-INF/resources'),
        emptyOutDir: true,
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
