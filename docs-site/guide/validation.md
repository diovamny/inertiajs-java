# Validation

Validation stays on the server. Inertia does not prescribe a schema language:
Bean Validation, a manual `BindingResult` check or any service-level rule all
work. The adapter normalizes the failure into the `errors` prop and redirects
back with the submitted input.

## The validation flow

```text
POST /contacts
  ├── valid   → mutate → 303 /contacts + flash.success
  └── invalid → 303 back   + errors + old input
```

A validation failure is never a `200` page response and never a `422` for a
normal form submit. The redirect keeps the browser refresh-safe. `422` is
reserved for Precognition requests.

## Spring Boot

### Form posts with `BindingResult`

```java
@PostMapping("/contacts")
public Object store(@Valid ContactForm form, BindingResult result) {
    if (result.hasErrors()) {
        return inertia.back()
            .withErrors(fieldErrors(result))
            .withInput(inputOf(form));
    }
    repository.create(form);
    return inertia.redirect("/contacts")
        .withFlash("success", "Contact created.");
}

private static Map<String, String> fieldErrors(BindingResult result) {
    var errors = new LinkedHashMap<String, String>();
    for (FieldError error : result.getFieldErrors()) {
        errors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    return errors;
}
```

### JSON bodies

For `@Valid @RequestBody`, the adapter's `PrecognitionHandler` catches
`MethodArgumentNotValidException` and redirects back with the errors already
flashed. You can still write an explicit handler when you need custom messages.

### The form record

```java
public record ContactForm(
    @NotBlank(message = "Name is required") String name,
    @NotBlank @Email(message = "Enter a valid email") String email,
    String phone
) {}
```

Bean Validation annotations are enforced only when the method parameter is
annotated with `@Valid` (or `@Validated` at class level).

## Quarkus

```java
@POST
public Uni<Object> store(@Valid ContactForm form) {
    repository.create(form);
    return inertia.redirect("/contacts")
        .withFlash("success", "Contact created.");
}
```

Add `quarkus-hibernate-validator` and keep `@Valid`. Validation failures are
mapped by the adapter's exception mapper: the client receives a `303` back
with `errors` and `old`, exactly like Spring.

For manual control:

```java
var violations = validator.validate(form);
if (!violations.isEmpty()) {
    return inertia.back()
        .withErrors(violations.stream().collect(
            Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (first, second) -> first)))
        .withInput(inputOf(form));
}
```

## Error wire shape

By default each field maps to a single message:

```json
{
  "errors": {
    "name": "Name is required",
    "email": "Enter a valid email"
  }
}
```

With `inertia.validation.all-errors=true`, every field maps to an ordered
array:

```json
{
  "errors": {
    "email": [
      "Enter a valid email",
      "Email is already registered"
    ]
  }
}
```

The flag applies consistently to flash errors, named bags and Precognition
`422` responses. The legacy single-message shape is the default so upgrading
does not change existing clients.

## Explicit error APIs

| API | Wire result |
|---|---|
| `withErrors(Map<String, String>)` | One message per field |
| `withValidationErrors(ValidationErrors)` | Ordered arrays, always |
| `withErrorMessages(Map<String, Collection<String>>)` | Ordered arrays, always |

```java
// Array shape regardless of the global flag
return inertia.back()
    .withErrorMessages(Map.of(
        "email", List.of("Enter a valid email", "Email is already registered")
    ))
    .withInput(inputOf(form));
```

## Error bags

Named bags let several forms coexist on one page. The client sends
`X-Inertia-Error-Bag: contact` and the server exposes:

```json
{
  "errors": {
    "contact": {
      "email": "Enter a valid email"
    }
  }
}
```

Without a bag the payload stays flat (`errors.email`). The bag is honored by:

- automatic validation exception handling,
- manual `withErrors` / `withValidationErrors` / `withErrorMessages`,
- the validation redirect itself,
- Precognition `422` filtering.

Client side:

```js
form.post('/contacts', { errorBag: 'contact' })
```

```vue
<Form action="/contacts" method="post" error-bag="contact">
  <!-- fields -->
</Form>
```

Bags are useful for an inbox with a quick-create form and a separate settings
form on the same screen; each form reads only its own errors.

## Old input

`withInput` / `withOldInput` stores the submitted payload for one request:

```java
return inertia.back()
    .withErrors(fieldErrors(result))
    .withInput(Map.of(
        "name", form.name(),
        "email", form.email(),
        "phone", form.phone()
    ));
```

The next page receives it as `props.old`, so the form can restore values
without trusting the client:

```vue
<input :value="old.name ?? contact?.name ?? ''" name="name" />
```

Never serialize password fields or other secrets into `old`.

## Client rendering

`useForm` reads `errors` automatically:

```vue
<p v-if="form.errors.email" class="error">
  {{ form.errors.email }}
</p>
```

With `all-errors` the value is an array; render every message:

```vue
<ul v-if="form.errors.email">
  <li v-for="message in form.errors.email" :key="message">
    {{ message }}
  </li>
</ul>
```

Use the same markup for both shapes by normalizing on the client, or opt into
arrays globally and type the errors accordingly.

## Precognition

Precognition validates a field without running the mutation. It is a separate
request with its own headers and a `422` response; see
[Precognition](/guide/precognition). The same Bean Validation annotations and
the same error shape are reused, so there is only one source of truth.

## Testing validation

::: code-group

```java [Spring Boot]
mvc.perform(post("/contacts")
        .param("name", "")
        .param("email", "not-an-email"))
    .andExpect(status().isSeeOther())
    .andExpect(redirectedUrl("/contacts/create"))
    .andExpect(flash().attributeExists("errors"));
```

```java [Quarkus]
given()
    .formParam("name", "")
    .formParam("email", "not-an-email")
    .redirects().follow(false)
    .when().post("/contacts")
    .then().statusCode(303)
    .header("Location", containsString("/contacts"));
```

:::

Assert the rendered page separately with `X-Inertia: true` to verify the
`errors` and `old` props, including the bag shape when relevant.

## Checklist

- [ ] Every validated endpoint uses `@Valid` and a message per constraint.
- [ ] Manual `BindingResult` checks redirect back with `withErrors` + `withInput`.
- [ ] Multi-message fields use `withValidationErrors` or `all-errors=true`.
- [ ] Named bags are tested with the `X-Inertia-Error-Bag` header.
- [ ] `old` input excludes passwords and tokens.
- [ ] Precognition and normal submits share the same rules.

Next: [Forms](/guide/forms) · [Precognition](/guide/precognition) ·
[Security](/guide/security)
