# Security

- CSRF: `XSRF-TOKEN` sync; Inertia failures are `303 + Location + flash`
  (same default both adapters).
- Redirect targets are validated before any header ships (`javascript:`,
  `data:`, CRLF rejected fail-closed).
- Pages beyond `inertia.max-page-bytes` (32 MiB) fail with `413`.
- Never serialize full JPA entities as props — use DTOs/records.
- Report vulnerabilities privately (see `SECURITY.md` in the repo).
