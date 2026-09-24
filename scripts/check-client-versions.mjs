import fs from 'node:fs';
import path from 'node:path';

// Pinned official-client check (release-0.0.5 Fase A).
// Fails unless every template + demo package.json declares exactly 3.7.1
// for @inertiajs/vue3|react|svelte AND every lockfile resolves 3.7.1.
// Run: node scripts/check-client-versions.mjs (wired into verify:metadata).
const root = process.cwd();
const EXPECTED = '3.7.1';
const PKGS = ['@inertiajs/vue3', '@inertiajs/react', '@inertiajs/svelte'];
const failures = [];
const ok = (msg) => console.log(`  ok: ${msg}`);
const fail = (msg) => failures.push(msg);

const manifests = [
  'archetypes/inertia-spring-vue-archetype/src/main/resources/archetype-resources/src/main/webui/package.json',
  'archetypes/inertia-spring-react-archetype/src/main/resources/archetype-resources/src/main/webui/package.json',
  'archetypes/inertia-spring-svelte-archetype/src/main/resources/archetype-resources/src/main/webui/package.json',
  'archetypes/inertia-quarkus-vue-archetype/src/main/resources/archetype-resources/src/main/webui/package.json',
  'archetypes/inertia-quarkus-react-archetype/src/main/resources/archetype-resources/src/main/webui/package.json',
  'archetypes/inertia-quarkus-svelte-archetype/src/main/resources/archetype-resources/src/main/webui/package.json',
  'examples/spring/spring-pingcrm/src/main/webui/package.json',
  'examples/spring/spring-pingcrm-svelte/src/main/webui/package.json',
  'examples/spring/spring-kitchen-sink/src/main/webui/package.json',
  'examples/quarkus/demo-app/src/main/webui/package.json',
  'examples/quarkus/kitchen-sink/src/main/webui/package.json',
  'examples/quarkus/pingcrm/src/main/webui/package.json',
  'examples/quarkus/pingcrm-react/src/main/webui/package.json',
  'examples/quarkus/quarkus-pingcrm-svelte/src/main/webui/package.json',
];

console.log(`Expected @inertiajs/* client: ${EXPECTED}`);
for (const rel of manifests) {
  let json;
  try {
    json = JSON.parse(fs.readFileSync(path.join(root, rel), 'utf8'));
  } catch (e) {
    fail(`${rel}: unreadable (${e.message})`);
    continue;
  }
  const declared = { ...(json.dependencies ?? {}), ...(json.devDependencies ?? {}) };
  const found = PKGS.filter((p) => declared[p] !== undefined);
  if (found.length === 0) {
    fail(`${rel}: no @inertiajs/* runtime adapter declared`);
    continue;
  }
  for (const p of found) {
    if (declared[p] !== EXPECTED) fail(`${rel}: ${p} is ${declared[p]}, expected exactly ${EXPECTED}`);
    else ok(`${rel}: ${p}@${EXPECTED}`);
  }
}

const locks = [
  'examples/spring/spring-pingcrm/src/main/webui/package-lock.json',
  'examples/spring/spring-pingcrm-svelte/src/main/webui/package-lock.json',
  'examples/spring/spring-kitchen-sink/src/main/webui/package-lock.json',
  'examples/quarkus/demo-app/src/main/webui/package-lock.json',
  'examples/quarkus/kitchen-sink/src/main/webui/package-lock.json',
  'examples/quarkus/pingcrm/src/main/webui/package-lock.json',
  'examples/quarkus/pingcrm-react/src/main/webui/package-lock.json',
  'examples/quarkus/quarkus-pingcrm-svelte/src/main/webui/package-lock.json',
];

for (const rel of locks) {
  let json;
  try {
    json = JSON.parse(fs.readFileSync(path.join(root, rel), 'utf8'));
  } catch (e) {
    fail(`${rel}: unreadable (${e.message})`);
    continue;
  }
  const pkgs = json.packages ?? {};
  const keys = Object.keys(pkgs).filter((k) => /^node_modules\/@inertiajs\/(vue3|react|svelte)$/.test(k));
  if (keys.length === 0) {
    fail(`${rel}: no locked @inertiajs/* adapter`);
    continue;
  }
  for (const k of keys) {
    const v = pkgs[k].version;
    if (v !== EXPECTED) fail(`${rel}: ${k} resolves ${v}, expected ${EXPECTED}`);
    else ok(`${rel}: ${k}=${EXPECTED}`);
  }
}

if (failures.length > 0) {
  console.error('Client version check FAILED:');
  for (const f of failures) console.error(` - ${f}`);
  process.exit(1);
}
console.log(`Client version check OK: all sources pinned at ${EXPECTED}.`);
