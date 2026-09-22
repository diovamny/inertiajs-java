import fs from 'node:fs';
import path from 'node:path';

// Generates docs/protocol-compatibility.md from specs/inertia-v3-compliance.yaml
// (PLAN v4 §3.5-§3.7 / H5). The markdown is a build artifact: CI regenerates it
// and fails on any diff. Also fails when a row is IMPLEMENTED without test_ref.
const root = process.cwd();
const yamlPath = path.join(root, 'specs', 'inertia-v3-compliance.yaml');
const outPath = path.join(root, 'docs', 'protocol-compatibility.md');

const STATUSES = new Set([
  'IMPLEMENTED',
  'TESTED',
  'E2E_VERIFIED',
  'NOT_SUPPORTED',
  'NOT_APPLICABLE',
]);

function parseFlowList(value) {
  const inner = value.trim().replace(/^\[/, '').replace(/\]$/, '');
  if (!inner.trim()) return [];
  return inner.split(',').map((s) => s.trim()).filter(Boolean);
}

function parseScalar(value) {
  const t = value.trim();
  if (t.startsWith('[') && t.endsWith(']')) return parseFlowList(t);
  if ((t.startsWith('"') && t.endsWith('"')) || (t.startsWith("'") && t.endsWith("'"))) {
    return t.slice(1, -1);
  }
  return t;
}

function parseYaml(text) {
  const lines = text.split(/\r?\n/);
  let updated = '';
  const entries = [];
  let current = null;
  for (const line of lines) {
    if (/^updated:\s*/.test(line)) {
      updated = line.replace(/^updated:\s*/, '').trim();
      continue;
    }
    const item = line.match(/^  - id:\s*(.+?)\s*$/);
    if (item) {
      current = { id: item[1] };
      entries.push(current);
      continue;
    }
    const kv = line.match(/^    ([A-Za-z0-9_]+):\s?(.*)$/);
    if (kv && current) {
      current[kv[1]] = parseScalar(kv[2] ?? '');
    }
  }
  return { updated, entries };
}

const { updated, entries } = parseYaml(fs.readFileSync(yamlPath, 'utf8'));

const failures = [];
const seen = new Set();
for (const e of entries) {
  if (seen.has(e.id)) failures.push(`duplicate id ${e.id}`);
  seen.add(e.id);
  if (!STATUSES.has(e.status)) failures.push(`${e.id}: unknown status ${e.status}`);
  if (e.status === 'IMPLEMENTED' && !e.test_ref) {
    failures.push(`${e.id}: IMPLEMENTED without test_ref`);
  }
  if (!e.description || !e.section) failures.push(`${e.id}: missing description/section`);
}
if (failures.length > 0) {
  console.error('Compliance YAML validation FAILED:');
  for (const f of failures) console.error(` - ${f}`);
  process.exit(1);
}

const applicable = entries.filter((e) => e.status !== 'NOT_APPLICABLE');
const verified = entries.filter((e) => e.status === 'TESTED' || e.status === 'E2E_VERIFIED');
const e2e = entries.filter((e) => Array.isArray(e.e2e_verified) && e.e2e_verified.length > 0);

function cell(status) {
  switch (status) {
    case 'TESTED':
    case 'E2E_VERIFIED':
      return '✅';
    case 'IMPLEMENTED':
      return '🔄';
    case 'NOT_SUPPORTED':
      return '❌';
    case 'NOT_APPLICABLE':
      return 'N/A';
    default:
      return '❓';
  }
}

function statusLabel(e) {
  if (e.status === 'TESTED' && Array.isArray(e.e2e_verified) && e.e2e_verified.length > 0) {
    return `TESTED (+E2E ${e.e2e_verified.join(', ')})`;
  }
  return e.status;
}

let md = `# Inertia v3 Conformance Matrix

**Date:** ${updated}
**Scope:** Spring Boot Inertia.js v3 Adapter & Quarkus Inertia.js v3 Adapter
**Reference:** [Inertia v3 Protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol)
**Source of truth:** [\`specs/inertia-v3-compliance.yaml\`](../specs/inertia-v3-compliance.yaml) — this file is GENERATED, do not edit by hand (see \`scripts/generate-compatibility-matrix.mjs\`).

[![Protocol v3](https://img.shields.io/badge/Inertia%20v3-${verified.length}%2F${applicable.length}%20verified-brightgreen)](../specs/inertia-v3-compliance.yaml)
**Protocol: ${verified.length}/${applicable.length} verified (${e2e.length} with Playwright E2E).**

Status semantics: ✅ means an executable test exists and is green in the current suite; 🔄 means the code exists with indirect coverage and a dedicated test is tracked (see notes); ❌ means the requirement is known but not yet backed by a real test; \`N/A\` means not applicable.

Important: this file is now evidence-based and generated. Claims of 100% compatibility are deliberately avoided until the E2E matrix and quality gates are green.

| # | Category | Requirement | Source | Status | Spring | Quarkus | Test | Notes |
|---|----------|-------------|--------|--------|--------|---------|------|-------|
`;

let n = 0;
for (const e of entries) {
  n += 1;
  const mark = cell(e.status);
  md += `| ${n} | ${e.section} | ${e.description} | ${e.reference} | ${statusLabel(e)} | ${mark} | ${mark} | ${e.test_ref} | ${e.notes} |\n`;
}

fs.writeFileSync(outPath, md);
console.log(`Matrix generated: ${entries.length} rows, ${verified.length}/${applicable.length} verified, ${e2e.length} E2E.`);
