# Asset versioning

Precedence: `version-custom` > `vite-manifest` > `sha256` (content hash) >
fallback. Mismatches answer `409 + X-Inertia-Location`; flash survives for
the follow-up full reload.
