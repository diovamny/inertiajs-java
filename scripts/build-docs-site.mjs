import path from 'node:path';

// Builds docs-site/ through the VitePress API (the local CLI wrapper hangs
// on some Windows machines; the API path is equivalent and CI uses the CLI).
const root = process.cwd();
const { build } = await import('vitepress');
await build(path.join(root, 'docs-site'));
console.log('Docs site build OK.');
