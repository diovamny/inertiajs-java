import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const matrixPath = path.join(root, 'docs', 'protocol-compatibility.md');
const text = fs.readFileSync(matrixPath, 'utf8');

const testNames = [...text.matchAll(/\|\s*\d+\s*\|.*?\|.*?\|.*?\|.*?\|.*?\|\s*([^|]+?)\s*\|/g)]
  .map((match) => match[1])
  .flatMap((cell) => cell.split(',').map((part) => part.trim()))
  .map((name) => name.replace(/\s*\(.*?\)\s*$/, '').trim())
  .filter(Boolean)
  .filter((name) => /^[A-Za-z0-9_]+$/.test(name));

const candidates = new Set();
for (const dir of ['spring-inertia/src/test/java', 'quarkus-inertia/src/test/java']) {
  const fullDir = path.join(root, dir);
  if (!fs.existsSync(fullDir)) continue;

  const walk = (current) => {
    for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
      const next = path.join(current, entry.name);
      if (entry.isDirectory()) walk(next);
      else if (entry.isFile() && entry.name.endsWith('.java')) {
        candidates.add(path.basename(entry.name, '.java'));
      }
    }
  };

  walk(fullDir);
}

const missing = [...new Set(testNames)].filter((name) => !candidates.has(name));

if (missing.length > 0) {
  console.error('Missing documented test classes:');
  for (const name of missing) console.error(` - ${name}`);
  process.exit(1);
}

console.log(`Matrix validation OK: ${testNames.length} test references resolved to existing Java test classes.`);
