# History

Inertia keeps a history entry per visit, including the page object, scroll
positions and optional local component state. The adapter controls three flags
on the page object: `encryptHistory`, `clearHistory` and `preserveFragment`.

## Page flags

| Flag | Page field | Effect |
|---|---|---|
| Encrypt history | `encryptHistory: true` | History state is stored encrypted in the browser |
| Clear history | `clearHistory: true` | Previous entries are discarded on the next visit |
| Preserve fragment | `preserveFragment: true` | The URL `#fragment` survives the visit |

All three default to `false` and are enforced by the official client, not by
the server.

## Configuration

```properties
inertia.encrypt-history=true
inertia.clear-history=true
```

`inertia.clear-history` is Spring-only in configuration; both adapters expose
the per-request API. Use the API when the behavior depends on the page:

::: code-group

```java [Spring Boot]
@GetMapping("/account/security")
public Object security() {
    inertia.setEncryptHistory(true);
    return inertia.render("Account/Security", Map.of());
}

@PostMapping("/logout")
public Object logout(HttpSession session) {
    session.invalidate();
    inertia.setClearHistory(true);
    return inertia.redirect("/login");
}
```

```java [Quarkus]
@GET
@Path("/account/security")
public Uni<Object> security() {
    inertia.encryptHistory(true);
    return inertia.render("Account/Security", Map.of());
}

@POST
@Path("/logout")
public Uni<Object> logout() {
    // Invalidate the identity with your security framework first.
    inertia.clearHistory(true);
    return inertia.redirect("/login");
}
```

:::

The method names differ slightly between adapters:

| Effect | Spring | Quarkus |
|---|---|---|
| Encrypt history | `setEncryptHistory(true)` | `encryptHistory(true)` |
| Clear history | `setClearHistory(true)` | `clearHistory(true)` |
| Preserve fragment | `preserveFragment(true)` | `preserveFragment(true)` |
| Override version | `setVersion("abc")` | `version("abc")` |

```java
// Spring
inertia.setEncryptHistory(true);
inertia.setClearHistory(false);
inertia.preserveFragment(true);
return inertia.render("Docs/Show", Map.of("doc", doc));

// Quarkus
inertia.encryptHistory(true);
inertia.clearHistory(false);
inertia.preserveFragment(true);
return inertia.render("Docs/Show", Map.of("doc", doc));
```

The page result is produced after these setters, so the flags appear on the
serialized page object exactly once.

## Encrypt history

History state contains the full page props, including data the user was allowed
to see at that moment. Encryption protects that snapshot if the browser storage
is inspected later, for example on a shared machine.

- Encryption happens in the browser with the Web Crypto API.
- The keys live in `sessionStorage`, so closing the tab removes the ability to
  decrypt old entries.
- The server only advertises `encryptHistory: true`; it does not receive or
  store the key.
- Enable it for pages with personal, financial or health data.
- It is not a substitute for authorization: a user who reloads the page still
  needs a valid session and server-side permission.

## Clear history

`clearHistory` tells the client to drop previous entries on the next visit. The
classic use is logout: after signing out, pressing Back must not reveal the
authenticated page from the history cache.

```java
@PostMapping("/logout")
public Object logout(HttpSession session) {
    session.invalidate();
    inertia.setClearHistory(true);
    return inertia.redirect("/login")
        .withFlash("success", "Signed out.");
}
```

`clearHistory` clears the Inertia history, not the browser's entire session
history. A hard refresh or a manually typed URL still goes through the server
and is protected by authentication.

## Preserve fragment

By default a visit drops the URL fragment. When the page uses anchor navigation,
ask the client to keep it:

```java
inertia.preserveFragment(true);
return inertia.render("Docs/Show", Map.of("doc", doc));
```

With `preserveFragment: true` the URL keeps `#installation` across the visit,
and the browser can scroll to the anchor after render. This is different from
`preserveScroll`: the fragment is part of the URL, the scroll position is part
of the history state.

## Remembered local state

The official client can remember component state across visits with
`router.remember` / `useRemember`. The server does not need to participate:

::: code-group

```js [Vue 3]
import { useRemember } from '@inertiajs/vue3'

const filters = useRemember({ search: '', page: 1 }, 'contacts-filters')
```

```js [React 19]
import { useRemember } from '@inertiajs/react'

const [filters, setFilters] = useRemember(
  { search: '', page: 1 },
  'contacts-filters',
)
```

```js [Svelte 5]
import { useRemember } from '@inertiajs/svelte'

const filters = useRemember({ search: '', page: 1 }, 'contacts-filters')
```

:::

Remembered state uses the same history entry as the page. With
`encryptHistory: true` it is encrypted along with the page object.

## History and redirects

- `inertia.redirect(url)` pushes a new entry by default.
- A `replace` visit replaces the current entry, which is what you want after a
  login or a successful create when Back should not return to the form.
- A `409 + X-Inertia-Location` response forces a full page load and a new
  browser history entry.
- A version mismatch also forces a full reload; flash data survives it.

```js
router.post('/login', data, { replace: true })
```

## Testing history flags

```java
// Spring
mvc.perform(get("/account/security").header("X-Inertia", "true"))
    .andExpect(status().isOk())
    .andExpect(InertiaResultMatchers.inertia().encryptHistory(true));
```

```java
// Quarkus
InertiaPage.fromJson(response.asString())
    .assertEncryptHistory(true)
    .assertClearHistory(false)
    .assertPreserveFragment(false);
```

For browser behavior, assert `history.length`, the URL fragment and the
`window.history.state.page.encryptHistory` flag through Playwright.

## Checklist

- [ ] Sensitive pages set `encryptHistory`.
- [ ] Logout sets `clearHistory` before redirecting.
- [ ] Login and create flows use `replace` where Back is undesirable.
- [ ] Fragment-based pages set `preserveFragment`.
- [ ] History is never treated as an authorization cache.
- [ ] Tests assert the flags on real page objects.

Next: [Asset Versioning](/guide/asset-versioning) · [Redirects](/guide/redirects) ·
[Security](/guide/security)
