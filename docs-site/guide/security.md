# Security

Inertia does not authenticate users and does not authorize requests. Spring
Security or Quarkus Security owns identity, roles and CSRF; the adapter only
translates security outcomes into protocol-correct responses so an SPA can
react without a white screen.

That division is deliberate. There is exactly **one CSRF owner** per
application, and authorization always happens on the server.

## Security modes

```properties
inertia.security.mode=auto
inertia.security.fail-on-fallback=false
```

| Mode | Owner of CSRF | Use when |
|---|---|---|
| `auto` | Framework if the integration artifact is present, otherwise adapter | Default |
| `framework` | Spring Security / `quarkus-rest-csrf` | Production with a security framework |
| `adapter` | The Inertia adapter | The application brings its own auth but no framework CSRF |
| `disabled` | None | Tests or cookie-less APIs only |

Resolution order:

1. An explicit `inertia.security.mode` always wins.
2. If it is unset, the legacy `inertia.csrf-enabled` alias maps `true` to
   `adapter` and `false` to `disabled`, with a deprecation warning.
3. If neither is set, `auto` applies.
4. In `auto`, the presence of Spring Security plus `spring-inertia-security`
   (or Quarkus Security plus `quarkus-inertia-security`) resolves to
   `framework`; otherwise the adapter fallback is used.
5. `framework` fails at startup when the framework or the integration artifact
   is missing.
6. `auto` with `fail-on-fallback=true` refuses to start if it resolves to
   `adapter`.
7. `disabled` is rejected in production unless
   `inertia.security.allow-disabled-in-production=true`.

::: danger Never ship `disabled`
`disabled` removes CSRF validation. It is appropriate for a test slice or a
stateless bearer-token API with no cookies, never for a browser application
that uses a session.
:::

## HTTP contract

| Situation | Full page load | Inertia visit (`X-Inertia: true`) |
|---|---|---|
| Anonymous, protected route | Framework redirect to login | `409` + `X-Inertia-Location` (no `X-Inertia`) |
| Authenticated, insufficient role | Framework `403` | Inertia `403` page (`Errors/Forbidden`, `X-Inertia: true`) |
| Missing/invalid CSRF token | Framework error | `303` back to the same-origin referer + flash |
| Valid mutation | Normal response | Normal page/redirect, `302` normalized to `303` |
| Asset version mismatch | Not applicable | `409` + `X-Inertia-Location` |

A `409` control response never carries `X-Inertia: true` and never carries a
page body: the client performs a full navigation. A `403` page is a real page
and does carry `X-Inertia: true`.

CSRF failure redirects only to a **same-origin** referer. Anything else falls
back to `inertia.security.csrf-failure-path` (default `/`). This prevents the
adapter from becoming an open redirector.

## Spring Security

Add the optional integration artifact:

```xml
<dependency>
  <groupId>io.github.diovamny.spring.inertia</groupId>
  <artifactId>spring-inertia-security</artifactId>
  <version>0.0.6</version>
</dependency>
```

It auto-configures helpers but does **not** create a `SecurityFilterChain`.
Your application keeps control of routes, login and authorization:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain app(HttpSecurity http,
            InertiaAuthenticationEntryPoint entryPoint,
            InertiaAccessDeniedHandler deniedHandler,
            CookieCsrfTokenRepository csrfRepository) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/assets/**", "/favicon.svg").permitAll()
                .requestMatchers("/contacts/**").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").permitAll())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler))
            .csrf(csrf -> InertiaCsrfConfigurer.configure(csrf, csrfRepository));

        return http.build();
    }

    @Bean
    CookieCsrfTokenRepository csrfRepository() {
        return InertiaCsrfConfigurer.xsrfTokenRepository("/");
    }
}
```

The helper configures `CookieCsrfTokenRepository` with cookie `XSRF-TOKEN`,
header `X-XSRF-TOKEN`, `HttpOnly=false` (the client must read it) and the raw
`CsrfTokenRequestAttributeHandler`. The XOR-masking handler is intentionally not
used: the official Inertia client echoes the raw cookie value.

| Bean | Responsibility |
|---|---|
| `InertiaAuthenticationEntryPoint` | `409 + X-Inertia-Location` for Inertia visits; framework redirect otherwise |
| `InertiaAccessDeniedHandler` | CSRF → `303` + flash; authorization → Inertia `403`; otherwise delegate |
| `InertiaCsrfTokenEmitFilter` | Persists the token on plain GET visits |
| `InertiaAuthPropsContributor` | Optional `auth.user` summary, presentation only |

Keep the adapter filter disabled in framework mode. Two CSRF validators produce
two tokens and unpredictable failures.

### Opt-in auth props

```properties
inertia.security.auth-props-enabled=true
```

This exposes an allowlisted summary:

```json
{
  "auth": {
    "user": {
      "id": "alice",
      "name": "alice",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

It never exposes the `Authentication` object, credentials, tokens or arbitrary
attributes. `auth.can.*` flags are UI hints; `@PreAuthorize` and URL rules remain
the authorization barrier.

## Quarkus Security

```xml
<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia-security</artifactId>
  <version>0.0.6</version>
</dependency>

<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-rest-csrf</artifactId>
</dependency>
```

`quarkus-rest-csrf` is the only CSRF validator in `framework` mode:

```properties
quarkus.rest-csrf.enabled=true
quarkus.rest-csrf.cookie-name=XSRF-TOKEN
quarkus.rest-csrf.token-header-name=X-XSRF-TOKEN
quarkus.rest-csrf.cookie-http-only=false
%prod.quarkus.rest-csrf.cookie-force-secure=true
```

::: warning Do not set a signed CSRF key
`quarkus-rest-csrf` with `quarkus.rest-csrf.token-signature-key` requires the
cookie value to equal the signed header value. Cookie-echoing SPA clients cannot
satisfy that, so leave the key unset for Inertia applications.
:::

The integration registers:

- identity summary props (`auth.user`) when
  `inertia.security.auth-props-enabled=true`;
- a bridge filter that converts Quarkus authentication challenges into
  `409 + X-Inertia-Location`;
- a `403` Inertia page for authorization failures;
- CSRF ownership checks that fail startup in `framework` mode when
  `quarkus-rest-csrf` is missing.

Reactive Routes need their mutating prefixes declared, because
`quarkus-rest-csrf` only sees JAX-RS endpoints:

```properties
inertia.security.reactive-csrf-paths=/contacts,/api
```

Prefer real Quarkus Security features over a hand-written `AuthFilter`:
`SecurityIdentity`, `@Authenticated`, `@RolesAllowed` and HTTP policies. A
manual filter that sets an identity from the session is not a security
integration.

## Adapter fallback

When no security framework is present, the adapter provides a bounded layer:

- a 256-bit CSRF token bound to the session, compared in constant time;
- the `XSRF-TOKEN` cookie with configurable `SameSite`, `Secure`, path and
  domain;
- `303` + flash on CSRF failure, same-origin referer only;
- safe JSON serialization and exception redaction.

It does **not** provide authentication, user storage, password hashing, MFA,
OIDC, authorization rules or brute-force protection. Set
`fail-on-fallback=true` in regulated environments so a misconfigured
deployment refuses to boot instead of silently using the fallback.

```properties
inertia.security.fail-on-fallback=true
```

## CSRF configuration reference

| Property | Default | Meaning |
|---|---|---|
| `inertia.security.mode` | `auto` | `auto`, `framework`, `adapter`, `disabled` |
| `inertia.security.fail-on-fallback` | `false` | Refuse `auto` → adapter |
| `inertia.security.allow-disabled-in-production` | `false` | Permit `disabled` with prod profile |
| `inertia.security.login-url` | `/login` | Target of the `409` challenge |
| `inertia.security.forbidden-component` | `Errors/Forbidden` | Inertia `403` component |
| `inertia.security.csrf-failure-path` | `/` | Fallback when the referer is not same-origin |
| `inertia.security.csrf-flash-key` | `error` | Flash key for the message |
| `inertia.security.csrf-flash-message` | localized default | Generic expiry message |
| `inertia.security.cookie-same-site` | `Lax` | XSRF cookie `SameSite` |
| `inertia.security.cookie-secure` | `false` | XSRF cookie `Secure` |
| `inertia.security.cookie-path` | `/` | XSRF cookie path |
| `inertia.security.cookie-domain` | empty | XSRF cookie domain |
| `inertia.security.reactive-csrf-paths` | empty | Quarkus reactive prefixes |
| `inertia.security.auth-props-enabled` | `false` | Contribute `auth.user` |
| `inertia.csrf-enabled` | unset | Deprecated alias for `mode` |

## Client configuration

With the defaults, no client code is needed: the official client reads
`XSRF-TOKEN` and sends `X-XSRF-TOKEN`. If you rename either, configure the
client to match:

```js
createInertiaApp({
  // ...
  http: {
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
  },
})
```

Do not write a second interceptor that adds the header; duplicated tokens are a
common source of intermittent `419` responses.

## Other protections

| Risk | Adapter behavior |
|---|---|
| Open redirects | `inertia.redirect` validates targets; `javascript:`, `data:`, CRLF rejected |
| Oversized pages | `inertia.max-page-bytes` (32 MiB) answers `413` |
| XSS in page JSON | JSON is safely escaped inside the `application/json` script |
| Error leakage | `inertia.error-details-enabled=false` outside development |
| CDN leakage | `Vary: X-Inertia` and token-dependent responses are not cached blindly |
| SSR trust | Sidecar is local by default; remote requires HTTPS + allowlist |

Never serialize full JPA entities or secrets as props. A prop is visible to the
user's browser; redaction in the UI is not access control.

## Testing security

```java
// Anonymous Inertia visit → 409 + location
mvc.perform(get("/contacts").header("X-Inertia", "true"))
    .andExpect(status().isConflict())
    .andExpect(header().string("X-Inertia-Location", "/login"))
    .andExpect(header().doesNotExist("X-Inertia"));

// Authenticated without role → 403 Inertia page
mvc.perform(get("/admin").header("X-Inertia", "true").with(user("bob").roles("USER")))
    .andExpect(status().isForbidden())
    .andExpect(header().string("X-Inertia", "true"));

// Mutating request without CSRF → 303 + flash
mvc.perform(post("/contacts").header("X-Inertia", "true").with(user("alice")))
    .andExpect(status().isSeeOther())
    .andExpect(flash().attributeExists("error"));
```

Add a test proving the controller is **not** invoked when CSRF fails. Asserting
only the status code can hide a filter-order regression.

## Checklist

- [ ] Exactly one CSRF owner is active.
- [ ] `security.mode` is explicit in production.
- [ ] `fail-on-fallback=true` where a framework is mandatory.
- [ ] Login challenge answers `409 + X-Inertia-Location`.
- [ ] Forbidden answers a real Inertia `403` page.
- [ ] CSRF failures redirect `303` with a generic flash message.
- [ ] XSRF cookie is `HttpOnly=false`, `Secure=true` under HTTPS.
- [ ] Authorization is enforced by the framework, never by props.
- [ ] Reactive route prefixes are declared for Quarkus.
- [ ] Secrets, tokens and full entities never appear in props.

Next: [Testing](/guide/testing) · [Native Image](/guide/native-image) ·
[Configuration](/reference/configuration)
