# Redirects

Inertia follows the Post/Redirect/Get (PRG) pattern. Mutations end with a
redirect, so refreshing the browser does not repeat the write. The adapter
chooses the status code and keeps flash messages and validation errors across
the redirect.

## Redirect semantics

| Situation | Status | Header/body |
|---|---|---|
| `GET` redirect | `302 Found` | `Location` |
| Mutation (`POST`, `PUT`, `PATCH`, `DELETE`) | `303 See Other` | `Location` |
| External URL during an Inertia visit | `409 Conflict` | `X-Inertia-Location` |
| Asset version mismatch | `409 Conflict` | `X-Inertia-Location` |
| CSRF failure on an Inertia visit | `303 See Other` | `Location` + flash |

`303` is important after a mutation: it tells the client to follow the redirect
with a `GET`, avoiding duplicate submissions.

## Basic redirect

::: code-group

```java [Spring Boot]
@PostMapping("/contacts")
public Object store(@Valid ContactForm form) {
    var contact = contacts.create(form);
    return inertia.redirect("/contacts/" + contact.id())
        .withFlash("success", "Contact created.");
}
```

```java [Quarkus]
@POST
public Uni<Object> store(@Valid ContactForm form) {
    var contact = contacts.create(form);
    return inertia.redirect("/contacts/" + contact.id())
        .withFlash("success", "Contact created.");
}
```

:::

`inertia.redirect(url)` returns a redirect result. On a full-page form
submission it becomes an HTTP redirect; on an Inertia visit it becomes `303`
with the `Location` header, and the client performs the next visit.

## Redirect back

Validation failures and "not found" cases usually return to the previous page:

```java
if (result.hasErrors()) {
    return inertia.back()
        .withErrors(fieldErrors(result))
        .withInput(inputOf(form));
}

if (contact == null) {
    return inertia.back("/contacts")
        .withFlash("error", "Contact not found.");
}
```

`back()` uses the `Referer` header when it is same-origin and falls back to `/`
or the provided fallback path. The redirect keeps the user on the form instead
of dropping them on a generic error page.

## Flash messages

Flash data is stored in the session for exactly one request:

```java
return inertia.redirect("/contacts")
    .withFlash("success", "Contact created.");

// Multiple keys
return inertia.redirect("/contacts")
    .withFlash(Map.of(
        "success", "Contact created.",
        "context", "crm"
    ));
```

The client reads them from `props.flash` (or from the keys configured with
`inertia.flash-keys`). `withFlash` and `flash` are aliases; `.with(key, value)`
is also available for redirect results.

Flash messages survive the redirect and the subsequent full reload, including
when the redirect is caused by a version mismatch.

## Validation errors

```java
return inertia.back()
    .withErrors(Map.of("email", "Enter a valid email address."))
    .withInput(Map.of("name", form.name(), "email", form.email()));
```

For multiple messages per field use `withValidationErrors` or
`withErrorMessages`; both always emit arrays. Named bags are selected by the
`X-Inertia-Error-Bag` request header and appear under
`props.errors.<bag>.<field>`. See [Validation](/guide/validation).

## External redirects

A normal `Location` header cannot be followed by the Inertia client, because it
would return HTML that the client cannot parse. Use `inertia.location` for
external URLs:

```java
return inertia.location("https://billing.example.com/portal");
```

The response is `409 Conflict` with `X-Inertia-Location`. The client performs a
full `window.location` navigation. The same mechanism is used for OIDC login
redirects and other cross-origin flows.

::: warning Do not return 302 for external URLs
A `302` to another origin during an XHR visit is followed by `fetch`, and the
client receives an HTML login page or an error document instead of a page
object. `inertia.location(...)` is the correct contract.
:::

## CSRF failures

When a mutating Inertia request carries a missing or invalid CSRF token, both
adapters default to:

```text
303 See Other
Location: <same-origin referer or />
Flash: "The page expired, please try again."
```

This lets the user retry without a technical error modal. Non-Inertia requests
keep their framework behavior (Spring passes through; the Quarkus reactive
pre-handler keeps `419`). See [Security](/guide/security).

## Fragments

By default the URL fragment is not preserved across visits. Enable it when the
page relies on anchor navigation:

```java
inertia.preserveFragment(true);
return inertia.render("Docs/Show", Map.of("doc", doc));
```

Or configure it globally when the application uses fragment-based navigation.
`preserveFragment` is serialized into the page object as `preserveFragment:
true` and enforced by the official client.

## Redirects and history

- `inertia.redirect` pushes a new history entry.
- Use `replace` on the client when the intermediate page should not remain in
  history (for example, after a successful login).
- `clearHistory` removes the previous entries; `encryptHistory` protects the
  state of sensitive pages.

See [History](/guide/history) for the full behavior.

Next: [Validation](/guide/validation) · [Security](/guide/security) ·
[History](/guide/history)
