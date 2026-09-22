# Testing

Fluent DSLs (`InertiaPage`, `InertiaResultMatchers`) plus the executable
TCK: 39 normative cases on Spring/JAX-RS, 21 on Reactive Routes, and a
Playwright browser suite against the kitchen-sink demos.

```bash
mvn -o test                                   # 714 unit/integration tests
npx playwright test                           # browser contracts (demos running)
node scripts/generate-compatibility-matrix.mjs
```
