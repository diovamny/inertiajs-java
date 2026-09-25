# Compatibility

Evidence-based matrix (v3 only, no legacy mode). Policy:
[compatibility policy](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/docs/COMPATIBILITY_POLICY.md).

The normative matrix is generated from
[`specs/inertia-v3-compliance.yaml`](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/specs/inertia-v3-compliance.yaml)
— every requirement carries transports, status and test evidence. Read the
full [generated matrix](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/docs/protocol-compatibility.md)
and [what is not supported](/not-supported).

Contract matrix: **60/62 verified**. E2E interop per
[`e2e-compliance.yaml`](https://github.com/diovamny/inertiajs-java/blob/release-0.0.5/specs/e2e-compliance.yaml):
Vue/Spring 10/10 green (M4a); remaining cells land in M4b — contract ✅ is
not interop ✅ until the E2E cell is green.
