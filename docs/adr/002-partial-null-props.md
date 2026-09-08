# ADR-002: Requested null props are kept on partial reloads

- Date: 2026-09-07
- Status: accepted

## Context

`PartialReloadProcessor` (both adapters) silently dropped props whose resolved
value was `null` during `only`/`except` filtering, so clients could not
distinguish "absent" from "null".

## Decision

Null is a real prop value, not an omission signal (lazy/optional/rescued flows
use dedicated marker types resolved upstream). Selected props — including
explicit nulls — are kept; exclusions still drop keys entirely.

## Consequences

- Matches the official adapters' behavior; covered by mirrored regression
  tests (`PartialReloadProcessorTest`, `PartialReloadProcessorUnitTest`) and a
  Playwright contract case.
- Quarkus additionally copies results into a null-tolerant unmodifiable map
  (`Map.copyOf` rejects null values).
