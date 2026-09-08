## Summary

<!-- What does this change do and why? -->

## Tests

- [ ] New behavior is covered by unit and/or integration tests on the affected adapter(s).
- [ ] Protocol changes are mirrored on **both** adapters with the same contract cases.
- [ ] `mvn clean test` is green (adapters); `node scripts/check-conformance-matrix.mjs` passes.

## Docs

- [ ] User-facing changes are documented (`docs/`, `README.md` if needed).
- [ ] `CHANGELOG.md` entry added (English, Keep a Changelog).

## Compatibility

- [ ] No breaking public API change, or the break is justified and documented in `docs/migration.md`.

## Security checklist

- [ ] No secrets, credentials, tokens or personal data included.
- [ ] No new network calls, deserialization paths or validation gaps without review.
