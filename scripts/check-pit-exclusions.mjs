// Verifies every @QuarkusTest class is excluded from PIT (they require the
// QuarkusClassLoader and crash minions). Convention: *QuarkusTest suffix is
// excluded by pattern; the rest must be listed explicitly in the root pom's
// excludedTestClasses. Fails closed with the offending class names.
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const pom = fs.readFileSync(path.join(root, 'pom.xml'), 'utf8');
const block = pom.match(/<excludedTestClasses>([\s\S]*?)<\/excludedTestClasses>/);
if (!block) {
  console.error('PIT exclusions check FAILED: no excludedTestClasses in root pom');
  process.exit(1);
}
const patterns = [...block[1].matchAll(/<param>(.*?)<\/param>/g)].map((m) => m[1].trim());
const globs = patterns.map((p) => new RegExp('^' + p.replace(/\./g, '\\.').replace(/\*/g, '.*') + '$'));

function* javaFiles(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name === 'node_modules' || entry.name === 'target') continue;
      yield* javaFiles(full);
    } else if (entry.name.endsWith('.java')) {
      yield full;
    }
  }
}

const offenders = [];
const modules = fs.readdirSync(root, { withFileTypes: true })
  .filter((e) => e.isDirectory())
  .map((e) => e.name);
for (const mod of modules) {
  const testDir = path.join(root, mod, 'src', 'test');
  if (!fs.existsSync(testDir)) continue;
  for (const file of javaFiles(testDir)) {
    const content = fs.readFileSync(file, 'utf8');
    if (!content.includes('@QuarkusTest')) continue;
    const pkg = (content.match(/^\s*package\s+([\w.]+)\s*;/m) || [])[1];
    const cls = path.basename(file, '.java');
    const fqn = pkg ? `${pkg}.${cls}` : cls;
    if (!globs.some((g) => g.test(fqn))) offenders.push(`${fqn} (${file})`);
  }
}
if (offenders.length > 0) {
  console.error('PIT exclusions check FAILED: @QuarkusTest classes not excluded from PIT:');
  for (const o of offenders) console.error(` - ${o}`);
  process.exit(1);
}
console.log(`PIT exclusions OK: ${patterns.length} patterns cover all @QuarkusTest classes.`);
