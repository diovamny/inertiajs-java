# Forms

Inertia forms are normal HTML forms handled by the client. The official
`useForm` helper tracks data, errors, processing state and upload progress while
the request still ends in a server redirect or a validation response.

The server contract is always the same:

1. `POST`/`PUT`/`PATCH`/`DELETE` reaches a controller.
2. Validation succeeds → mutate, then redirect with flash data (`303`).
3. Validation fails → redirect back with `errors` and `old` input.

## Server side

::: code-group

```java [Spring Boot]
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
```

```java [Quarkus]
@POST
public Uni<Object> store(@Valid ContactForm form) {
    repository.create(form);
    return inertia.redirect("/contacts")
        .withFlash("success", "Contact created.");
}
```

:::

`withInput` makes the submitted values available as `props.old`, so the form
can repopulate itself after a validation redirect. The starters rely on this
instead of client-side schemas.

## `useForm` in Vue

```vue
<script setup lang="ts">
import { useForm } from '@inertiajs/vue3'

const props = defineProps<{ contact?: Contact }>()

const form = useForm({
  name: props.contact?.name ?? '',
  email: props.contact?.email ?? '',
  phone: props.contact?.phone ?? '',
})

function submit() {
  if (props.contact) {
    form.put(`/contacts/${props.contact.id}`)
  } else {
    form.post('/contacts')
  }
}
</script>

<template>
  <form @submit.prevent="submit">
    <label for="name">Name</label>
    <input id="name" v-model="form.name" />
    <p v-if="form.errors.name" class="error">{{ form.errors.name }}</p>

    <label for="email">Email</label>
    <input id="email" type="email" v-model="form.email" />
    <p v-if="form.errors.email" class="error">{{ form.errors.email }}</p>

    <button type="submit" :disabled="form.processing">
      {{ form.processing ? 'Saving…' : 'Save' }}
    </button>
  </form>
</template>
```

## `useForm` in React

```tsx
import { useForm } from '@inertiajs/react'

export default function Create() {
  const { data, setData, post, processing, errors, reset } = useForm({
    name: '',
    email: '',
    phone: '',
  })

  function submit(event: React.FormEvent) {
    event.preventDefault()
    post('/contacts', {
      onSuccess: () => reset(),
    })
  }

  return (
    <form onSubmit={submit}>
      <label htmlFor="name">Name</label>
      <input
        id="name"
        value={data.name}
        onChange={(e) => setData('name', e.target.value)}
      />
      {errors.name && <p className="error">{errors.name}</p>}

      <label htmlFor="email">Email</label>
      <input
        id="email"
        type="email"
        value={data.email}
        onChange={(e) => setData('email', e.target.value)}
      />
      {errors.email && <p className="error">{errors.email}</p>}

      <button type="submit" disabled={processing}>
        {processing ? 'Saving…' : 'Save'}
      </button>
    </form>
  )
}
```

## `useForm` in Svelte

```svelte
<script lang="ts">
  import { useForm } from '@inertiajs/svelte'

  const form = useForm({
    name: '',
    email: '',
    phone: '',
  })

  function submit(event: SubmitEvent) {
    event.preventDefault()
    form.post('/contacts')
  }
</script>

<form onsubmit={submit}>
  <label for="name">Name</label>
  <input id="name" bind:value={form.name} />
  {#if form.errors.name}<p class="error">{form.errors.name}</p>{/if}

  <button type="submit" disabled={form.processing}>
    {form.processing ? 'Saving…' : 'Save'}
  </button>
</form>
```

## The `<Form>` component

For simple forms the official `<Form>` component removes most boilerplate. It
reads the server errors itself and tracks processing:

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { Form } from '@inertiajs/vue3'
</script>

<template>
  <Form action="/contacts" method="post" v-slot="{ errors, processing }">
    <input name="name" placeholder="Name" />
    <p v-if="errors.name" class="error">{{ errors.name }}</p>
    <input name="email" type="email" placeholder="Email" />
    <button type="submit" :disabled="processing">Save</button>
  </Form>
</template>
```

```tsx [React 19]
import { Form } from '@inertiajs/react'

<Form action="/contacts" method="post">
  {({ errors, processing }) => (
    <>
      <input name="name" placeholder="Name" />
      {errors.name && <p className="error">{errors.name}</p>}
      <input name="email" type="email" placeholder="Email" />
      <button type="submit" disabled={processing}>Save</button>
    </>
  )}
</Form>
```

```svelte [Svelte 5]
<script lang="ts">
  import { Form } from '@inertiajs/svelte'
</script>

<Form action="/contacts" method="post" let:errors let:processing>
  <input name="name" placeholder="Name" />
  {#if errors.name}<p class="error">{errors.name}</p>{/if}
  <input name="email" type="email" placeholder="Email" />
  <button type="submit" disabled={processing}>Save</button>
</Form>
```

:::

The component expects plain `name` attributes and serializes the form itself.
Use `useForm` when you need transforms, watched fields or programmatic resets.

## `useForm` state

| Field | Meaning |
|---|---|
| `data` | Current form values |
| `errors` | Server validation errors keyed by field |
| `processing` | Request in flight |
| `progress` | Upload progress (`percentage`, `loaded`, `total`) |
| `recentlySuccessful` | True for two seconds after success |
| `isDirty` | Data differs from defaults |
| `hasErrors` | At least one error is present |
| `validating` | A precognition request is in flight |

| Method | Effect |
|---|---|
| `setData(key, value)` | Updates one field (React/Svelte style) |
| `post/put/patch/delete(url, options)` | Submits with the current data |
| `reset(...fields)` | Restores defaults (all or selected fields) |
| `clearErrors(...fields)` | Clears one or more errors |
| `transform(fn)` | Reshapes the payload before sending |
| `validate(field)` | Runs a Precognition validation request |
| `touch(...fields)` | Marks fields as touched for validation UI |

## Redirects and flash

A successful mutation never returns a page object. The client follows the
redirect and the next page receives `flash`:

```java
return inertia.redirect("/contacts")
    .withFlash("success", "Contact created.");
```

```vue
<div v-if="$page.props.flash?.success" class="flash">
  {{ $page.props.flash.success }}
</div>
```

The redirect is `303 See Other` after a mutation, so a browser refresh repeats
the `GET` and not the write.

## Transform before submit

Use `transform` when the wire payload differs from the form state:

```js
form.transform((data) => ({
  ...data,
  phone: data.phone || null,
  tags: data.tags.split(',').map((tag) => tag.trim()),
})).post('/contacts')
```

`transform` is applied for that submission only. Call `form.transform(null)`
(or a new transform) before the next one.

## File uploads

`useForm` sends multipart data automatically when a `File` is present. Track
progress with `form.progress` and see [File Uploads](/guide/file-uploads) for
the Spring `MultipartFile` and Quarkus `FileUpload` endpoints.

## Optimistic UI

`<Form>` supports an `optimistic` callback that merges predicted props while
the request is in flight:

```vue
<Form
  action="/contacts"
  method="post"
  :optimistic="(props, data) => ({
    contacts: [...props.contacts, { ...data, id: 'pending' }],
  })"
>
  <!-- fields -->
</Form>
```

A failure discards the optimistic state and renders the real response.

## Testing forms

```java
// Spring: validation redirect
mvc.perform(post("/contacts")
        .param("name", "")
        .param("email", "not-an-email"))
    .andExpect(status().isSeeOther())
    .andExpect(redirectedUrl("/contacts/create"))
    .andExpect(flash().attributeExists("errors"));

// Quarkus: successful create
given()
    .formParam("name", "Ada")
    .formParam("email", "ada@example.com")
    .when().post("/contacts")
    .then().statusCode(303);
```

## Checklist

- [ ] Every mutating form ends in `inertia.redirect(...)` or `inertia.back()`.
- [ ] Validation failures include `withErrors` and `withInput`.
- [ ] The submit button is disabled while `processing`.
- [ ] Server errors are rendered next to their fields.
- [ ] A successful create/edit form resets or navigates as intended.
- [ ] Multipart forms include CSRF and use the correct `enctype`.

Next: [Validation](/guide/validation) · [Precognition](/guide/precognition) ·
[File Uploads](/guide/file-uploads)
