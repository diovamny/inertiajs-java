# Precognition

Precognition validates form fields on the server **before** the user submits,
without executing the mutation. The controller runs its validation rules,
answers `422` with field errors or `204` on success, and never persists
anything.

It is not a second validation system: it reuses the same Bean Validation
annotations, the same error-message extraction and the same error bags as a real
submit.

## Request headers

| Header | Alias sent by clients | Meaning |
|---|---|---|
| `Precognition: true` | `X-Inertia-Precognition: true` | This is a precognition request |
| `Precognition-Validate-Only: email,name` | `X-Inertia-Precognition-Validate-Fields` | Validate only these fields |

Both spellings are accepted. The validate-only list filters the response so a
user editing one field does not see sibling errors.

## Server behavior

| Situation | Status | Headers | Body |
|---|---|---|---|
| Validate-only, no errors | `204 No Content` | `Precognition: true`, `Precognition-Success: true` | Empty |
| Validate-only, errors | `422 Unprocessable Content` | `Precognition: true` | `{"errors": {...}}` |
| Full precognition, no errors | `204 No Content` | `Precognition-Success: true` | Empty |
| Full precognition, errors | `422` | `Precognition: true` | `{"errors": {...}}` |

A precognition visit never returns a redirect, never writes flash data and never
renders the root template. The `Vary` header includes `Precognition` so caches
keep the two modes apart.

## Spring Boot

Validation is handled automatically for `@Valid @RequestBody` and
`@Validated` method parameters:

```java
@PostMapping("/contacts")
public Object store(@Valid @RequestBody ContactForm form) {
    repository.create(form);
    return inertia.redirect("/contacts")
        .withFlash("success", "Contact created.");
}
```

For classic form posts with a `BindingResult`, perform the same check and return
`422` explicitly when the request is a precognition request:

```java
@PostMapping("/contacts")
public Object store(@Valid ContactForm form, BindingResult result) {
    if (result.hasErrors()) {
        if (PrecognitionGuard.isPrecognitionRequest()) {
            return ResponseEntity.unprocessableContent()
                .body(Map.of("errors", fieldErrors(result)));
        }
        return inertia.back()
            .withErrors(fieldErrors(result))
            .withInput(inputOf(form));
    }
    repository.create(form);
    return inertia.redirect("/contacts");
}
```

The adapter's `PrecognitionHandler` already produces the `422` payload with the
right headers for `MethodArgumentNotValidException` and
`ConstraintViolationException`; the explicit branch is only needed for manual
`BindingResult` handling.

## Quarkus

```java
@POST
public Uni<Object> store(@Valid ContactForm form) {
    repository.create(form);
    return inertia.redirect("/contacts")
        .withFlash("success", "Contact created.");
}
```

Validation failures are mapped to the same `422` shape by the adapter. If you
validate manually, return the errors through the provided exception mapper or
build the response with `Response.status(422)` and the `Precognition` header.

## Read-only guarantee

Precognition requests are dry runs. Call the guard at the top of any write path
that could run before validation finishes:

```java
import io.github.diovamny.spring.inertia.validation.PrecognitionGuard;

@PostMapping("/contacts")
public Object store(@Valid ContactForm form, BindingResult result) {
    PrecognitionGuard.assertMutationsAllowed(); // throws on validate-only
    // ...
}
```

`assertMutationsAllowed()` throws `PrecognitionWriteBlockedException` when the
request is a validate-only precognition request. Put it in services that are
shared by several controllers to make the guarantee global, not per endpoint.

## Client with `useForm`

The official `useForm` returns a precognitive form when constructed with a
method and URL:

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { useForm } from '@inertiajs/vue3'

const form = useForm('post', '/contacts', {
  name: '',
  email: '',
  phone: '',
})
</script>

<template>
  <form @submit.prevent="form.post('/contacts')">
    <input
      v-model="form.name"
      @blur="form.validate('name')"
    />
    <p v-if="form.invalid('name')" class="error">
      {{ form.errors.name }}
    </p>

    <input
      v-model="form.email"
      @blur="form.validate('email')"
    />
    <p v-if="form.invalid('email')" class="error">
      {{ form.errors.email }}
    </p>

    <button type="submit" :disabled="form.processing || form.validating">
      Save
    </button>
  </form>
</template>
```

```tsx [React 19]
import { useForm } from '@inertiajs/react'

export default function Create() {
  const form = useForm('post', '/contacts', {
    name: '',
    email: '',
    phone: '',
  })

  return (
    <form onSubmit={(e) => { e.preventDefault(); form.post('/contacts') }}>
      <input
        value={form.data.name}
        onChange={(e) => form.setData('name', e.target.value)}
        onBlur={() => form.validate('name')}
      />
      {form.invalid('name') && <p className="error">{form.errors.name}</p>}

      <input
        value={form.data.email}
        onChange={(e) => form.setData('email', e.target.value)}
        onBlur={() => form.validate('email')}
      />
      {form.invalid('email') && <p className="error">{form.errors.email}</p>}

      <button type="submit" disabled={form.processing || form.validating}>
        Save
      </button>
    </form>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  import { useForm } from '@inertiajs/svelte'

  const form = useForm('post', '/contacts', {
    name: '',
    email: '',
    phone: '',
  })
</script>

<form onsubmit={(e) => { e.preventDefault(); form.post('/contacts') }}>
  <input
    bind:value={form.name}
    onblur={() => form.validate('name')}
  />
  {#if form.invalid('name')}<p class="error">{form.errors.name}</p>{/if}

  <button type="submit" disabled={form.processing || form.validating}>
    Save
  </button>
</form>
```

:::

| Method | Meaning |
|---|---|
| `validate(field)` | Sends a precognition request for one field |
| `validate()` | Validates every field |
| `valid(field)` | No error for that field |
| `invalid(field)` | An error exists for that field |
| `touched(field)` | The field was validated at least once |
| `validating` | A precognition request is in flight |
| `setValidationTimeout(ms)` | Debounce between validation requests |

Use `form.withAllErrors()` when the server emits multi-message arrays.

## Client without `useForm`

A plain fetch must send the headers explicitly:

```js
async function validateEmail(email) {
  const response = await fetch('/contacts', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      'Precognition': 'true',
      'Precognition-Validate-Only': 'email',
      'X-XSRF-TOKEN': readCookie('XSRF-TOKEN'),
    },
    body: JSON.stringify({ name: 'Ada', email }),
  })

  if (response.status === 204) return null
  const { errors } = await response.json()
  return errors.email
}
```

Do not send `X-Inertia: true` on a precognition request unless you are using the
official client flow; the response is a validation payload, not a page object.

## Testing

```java
// Spring: validate-only success
mvc.perform(post("/contacts")
        .header("Precognition", "true")
        .header("Precognition-Validate-Only", "email")
        .contentType(APPLICATION_JSON)
        .content("""
            {"name":"Ada","email":"ada@example.com"}
            """))
    .andExpect(status().isNoContent())
    .andExpect(header().string("Precognition-Success", "true"));

// Spring: validate-only failure
mvc.perform(post("/contacts")
        .header("Precognition", "true")
        .header("Precognition-Validate-Only", "email")
        .contentType(APPLICATION_JSON)
        .content("""
            {"name":"Ada","email":"nope"}
            """))
    .andExpect(status().isUnprocessableContent())
    .andExpect(jsonPath("$.errors.email").exists());
```

The TCK suite `07-validation.yaml` covers precognition and bags on all three
transports.

## Checklist

- [ ] Validation rules live in one place and serve both submit and precognition.
- [ ] Validate-only requests filter errors to the requested fields.
- [ ] Write paths call `PrecognitionGuard.assertMutationsAllowed()`.
- [ ] Precognition responses never redirect, flash or mutate.
- [ ] Client debounces validation and shows a validating state.
- [ ] `Precognition` is included in `Vary` for cache correctness.

Next: [Validation](/guide/validation) · [Forms](/guide/forms) ·
[Testing](/guide/testing)
