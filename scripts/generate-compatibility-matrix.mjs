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
  'IMPLEMENTADO',
  'TESTED',
  'TCK_VERIFICADO',
  'E2E_VERIFIED',
  'E2E_VERIFICADO',
  'NOT_SUPPORTED',
  'NOT_APPLICABLE',
  'NO_APLICA',
  'PARCIAL',
  'NO_EVALUADO',
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
  if ((e.status === 'IMPLEMENTED' || e.status === 'IMPLEMENTADO') && !e.test_ref) {
    failures.push(`${e.id}: IMPLEMENTADO without test_ref`);
  }
  if (!e.description || !e.section) failures.push(`${e.id}: missing description/section`);
}
if (failures.length > 0) {
  console.error('Compliance YAML validation FAILED:');
  for (const f of failures) console.error(` - ${f}`);
  process.exit(1);
}

const applicable = entries.filter((e) => e.status !== 'NOT_APPLICABLE' && e.status !== 'NO_APLICA');
const verified = entries.filter((e) => e.status === 'TESTED' || e.status === 'TCK_VERIFICADO' || e.status === 'E2E_VERIFIED' || e.status === 'E2E_VERIFICADO');
const e2e = entries.filter((e) => Array.isArray(e.e2e_verified) && e.e2e_verified.length > 0);

// Fail-closed I100: E2E_VERIFICADO without linked e2e_verified cells is rejected.
for (const e of entries) {
  if ((e.status === 'E2E_VERIFIED' || e.status === 'E2E_VERIFICADO')
      && (!Array.isArray(e.e2e_verified) || e.e2e_verified.length === 0)) {
    failures.push(`${e.id}: E2E_VERIFICADO without e2e_verified cells`);
  }
}

// I100 is computed from specs/e2e-compliance.yaml verified_cells only —
// contract ✅ never counts as interop.
let i100Total = 0;
let i100Verified = 0;
try {
  const e2eText = fs.readFileSync(path.join(root, 'specs', 'e2e-compliance.yaml'), 'utf8');
  const cellLists = [...e2eText.matchAll(/^\s*cells:\s*\[(.*?)\]\s*$/gm)].map((m) =>
    m[1].split(',').map((s) => s.trim()).filter(Boolean));
  const verifiedLists = [...e2eText.matchAll(/^\s*verified_cells:\s*\[(.*?)\]\s*$/gm)].map((m) =>
    m[1].split(',').map((s) => s.trim()).filter(Boolean));
  if (cellLists.length !== verifiedLists.length) {
    failures.push(`e2e-compliance.yaml: ${cellLists.length} cells lists vs ${verifiedLists.length} verified_cells lists`);
  }
  cellLists.forEach((cells, i) => {
    const allowed = new Set(cells);
    const seen = new Set();
    for (const c of verifiedLists[i] ?? []) {
      if (!allowed.has(c)) failures.push(`e2e-compliance.yaml: verified cell '${c}' not in scenario cells`);
      if (seen.has(c)) failures.push(`e2e-compliance.yaml: duplicate verified cell '${c}'`);
      seen.add(c);
      i100Verified++;
    }
    i100Total += cells.length;
  });
} catch (e) {
  failures.push(`e2e-compliance.yaml unreadable: ${e.message}`);
}
if (failures.length > 0) {
  console.error('Compliance YAML validation FAILED:');
  for (const f of failures) console.error(` - ${f}`);
  process.exit(1);
}

function cell(status) {
  switch (status) {
    case 'TESTED':
    case 'TCK_VERIFICADO':
    case 'E2E_VERIFIED':
    case 'E2E_VERIFICADO':
      return '✅';
    case 'IMPLEMENTED':
    case 'IMPLEMENTADO':
    case 'PARCIAL':
      return '🔄';
    case 'NOT_SUPPORTED':
    case 'NO_EVALUADO':
      return '❌';
    case 'NOT_APPLICABLE':
    case 'NO_APLICA':
      return 'N/A';
    default:
      return '❓';
  }
}

function e2eCell(e) {
  if (e.status === 'E2E_VERIFIED' || e.status === 'E2E_VERIFICADO') {
    return `✅ ${e.e2e_verified.join(', ')}`;
  }
  if (Array.isArray(e.e2e_verified) && e.e2e_verified.length > 0) {
    return `🧪 ${e.e2e_verified.join(', ')}`;
  }
  return '—';
}

function statusLabel(e) {
  if ((e.status === 'TESTED' || e.status === 'TCK_VERIFICADO') && Array.isArray(e.e2e_verified) && e.e2e_verified.length > 0) {
    return `${e.status} (+E2E ${e.e2e_verified.join(', ')})`;
  }
  return e.status;
}

let md = `# Inertia v3 Conformance Matrix

**Date:** ${updated}
**Scope:** Spring Boot Inertia.js v3 Adapter & Quarkus Inertia.js v3 Adapter
**Reference:** [Inertia v3 Protocol](https://inertiajs.com/docs/v3/core-concepts/the-protocol)
**Source of truth:** [\`specs/inertia-v3-compliance.yaml\`](../specs/inertia-v3-compliance.yaml) + [\`specs/e2e-compliance.yaml\`](../specs/e2e-compliance.yaml) — this file is GENERATED, do not edit by hand (see \`scripts/generate-compatibility-matrix.mjs\`).

[![C100 contract](https://img.shields.io/badge/C100-${verified.length}%2F${applicable.length}%20contract--verified-brightgreen)](../specs/inertia-v3-compliance.yaml)
[![I100 interop](https://img.shields.io/badge/I100-${i100Verified}%2F${i100Total}%20cells-green)](../specs/e2e-compliance.yaml)
**C100 contract: ${verified.length}/${applicable.length} TCK-verified. I100 interop: ${i100Verified}/${i100Total} E2E cells green with official clients.**

Status semantics (contract vs interop are separate): ✅ = normative test green on all 3 transports (counts for C100 only); 🧪 = officially observed by a real client in the listed transports (partial I100 evidence); 🔄 = code exists with indirect coverage and a tracked test; ❌ = known but untested; \`N/A\` = not applicable. A contract ✅ never counts as interop.

Important: this file is now evidence-based and generated. Claims of 100% compatibility are deliberately avoided until C100, I100 and the quality gates are all green.

| # | Category | Requirement | Source | Status | Spring | Quarkus | E2E (official clients) | Test | Notes |
|---|----------|-------------|--------|--------|--------|---------|------------------------|------|-------|
`;

let n = 0;
for (const e of entries) {
  n += 1;
  const mark = cell(e.status);
  md += `| ${n} | ${e.section} | ${e.description} | ${e.reference} | ${statusLabel(e)} | ${mark} | ${mark} | ${e2eCell(e)} | ${e.test_ref} | ${e.notes} |\n`;
}

fs.writeFileSync(outPath, md);
console.log(`Matrix generated: ${entries.length} rows, C100 ${verified.length}/${applicable.length}, I100 ${i100Verified}/${i100Total}.`);
