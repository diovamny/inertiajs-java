import fs from 'node:fs';
import path from 'node:path';

// Single-source version check (PLAN v4 §3.1 / H4).
// Fails when version.properties, root pom, module parents, archetype
// defaults, examples, README, CHANGELOG or ROADMAP disagree.
const root = process.cwd();
const failures = [];
const ok = (msg) => console.log(`  ok: ${msg}`);
const fail = (msg) => failures.push(msg);

const versionProps = fs.readFileSync(path.join(root, 'version.properties'), 'utf8');
const expected = (versionProps.match(/^project\.version\s*=\s*(.+?)\s*$/m) || [])[1];
if (!expected) {
  console.error('version.properties: missing project.version');
  process.exit(1);
}
console.log(`Expected version: ${expected}`);

const read = (rel) => fs.readFileSync(path.join(root, rel), 'utf8');

// 1. Root pom <version>.
const rootPom = read('pom.xml');
const rootVersion = (rootPom.match(/<artifactId>inertia-parent<\/artifactId>\s*<version>([^<]+)<\/version>/) || [])[1];
if (rootVersion !== expected) fail(`pom.xml root version is ${rootVersion}, expected ${expected}`);
else ok('pom.xml root version');

// 2. Module parent versions + own versions.
const modulePoms = [
  'inertia-core/pom.xml',
  'inertia-tck/pom.xml',
  'spring-inertia/pom.xml',
  'quarkus-inertia/pom.xml',
  'spring-inertia-security/pom.xml',
  'quarkus-inertia-security/pom.xml',
  'archetypes/pom.xml',
  'examples/pom.xml',
  'examples/spring/pom.xml',
  'examples/quarkus/pom.xml',
];
for (const rel of modulePoms) {
  const text = read(rel);
  const parents = [...text.matchAll(/<parent>[\s\S]*?<version>([^<]+)<\/version>/g)].map((m) => m[1]);
  for (const v of parents) {
    if (v !== expected) fail(`${rel}: parent version ${v}, expected ${expected}`);
  }
  if (parents.length > 0) ok(`${rel}: parent version`);
}

// 3. Archetype template defaults.
const archetypeMeta = [
  'archetypes/inertia-spring-vue-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml',
  'archetypes/inertia-spring-react-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml',
  'archetypes/inertia-spring-svelte-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml',
  'archetypes/inertia-quarkus-vue-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml',
  'archetypes/inertia-quarkus-react-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml',
  'archetypes/inertia-quarkus-svelte-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml',
];
for (const rel of archetypeMeta) {
  const text = read(rel);
  if (!text.includes(`<defaultValue>${expected}</defaultValue>`)) {
    fail(`${rel}: missing <defaultValue>${expected}</defaultValue>`);
  } else ok(`${rel}: defaultValue`);
}

// 3b. Archetype templates must declare explicit inertia-core pinned to the
// adapter version (Fase B): same core everywhere, no transitive drift.
const archetypeTemplatePoms = [
  'archetypes/inertia-spring-vue-archetype/src/main/resources/archetype-resources/pom.xml',
  'archetypes/inertia-spring-react-archetype/src/main/resources/archetype-resources/pom.xml',
  'archetypes/inertia-spring-svelte-archetype/src/main/resources/archetype-resources/pom.xml',
  'archetypes/inertia-quarkus-vue-archetype/src/main/resources/archetype-resources/pom.xml',
  'archetypes/inertia-quarkus-react-archetype/src/main/resources/archetype-resources/pom.xml',
  'archetypes/inertia-quarkus-svelte-archetype/src/main/resources/archetype-resources/pom.xml',
];
for (const rel of archetypeTemplatePoms) {
  const text = read(rel);
  if (!text.includes('<artifactId>inertia-core</artifactId>')) {
    fail(`${rel}: missing explicit inertia-core dependency`);
  } else if (!text.includes('<inertia-core.version>${inertiaAdapterVersion}</inertia-core.version>')) {
    fail(`${rel}: inertia-core.version must derive from \${inertiaAdapterVersion}`);
  } else ok(`${rel}: inertia-core pinned`);
}

// 4. Examples inertia.version pins.
const examplePoms = [
  'examples/spring/spring-pingcrm/pom.xml',
  'examples/spring/spring-pingcrm-react/pom.xml',
  'examples/spring/spring-pingcrm-svelte/pom.xml',
  'examples/spring/spring-kitchen-sink/pom.xml',
  'examples/quarkus/pingcrm/pom.xml',
  'examples/quarkus/pingcrm-react/pom.xml',
  'examples/quarkus/demo-app/pom.xml',
  'examples/quarkus/kitchen-sink/pom.xml',
];
for (const rel of examplePoms) {
  const text = read(rel);
  const pins = [...text.matchAll(/<inertia\.version>([^<]+)<\/inertia\.version>/g)].map((m) => m[1]);
  for (const v of pins) {
    if (v !== expected) fail(`${rel}: inertia.version ${v}, expected ${expected}`);
  }
  if (pins.length > 0) ok(`${rel}: inertia.version pin`);
}

// 5. README dependency + archetype versions.
const readme = read('README.md');
if (!readme.includes(`<version>${expected}</version>`)) fail(`README.md: missing <version>${expected}</version>`);
else ok('README.md dependency version');
if (!readme.includes(`-DarchetypeVersion=${expected}`)) fail(`README.md: missing -DarchetypeVersion=${expected}`);
else ok('README.md archetype version');

// 6. CHANGELOG top entry.
const changelog = read('CHANGELOG.md');
if (!changelog.includes(`## ${expected}`)) fail(`CHANGELOG.md: missing "## ${expected}" entry`);
else ok('CHANGELOG.md entry');

// 7. ROADMAP must not target an older release.
const roadmap = read('ROADMAP.md');
if (/Publish\s+0\.0\.[0-3]\s+to Maven Central/.test(roadmap)) {
  fail('ROADMAP.md: still targets an older release (Publish 0.0.x)');
} else ok('ROADMAP.md release target');

if (failures.length > 0) {
  console.error('Version check FAILED:');
  for (const f of failures) console.error(` - ${f}`);
  process.exit(1);
}
console.log(`Version check OK: all sources agree on ${expected}.`);
