# File Uploads

File uploads use normal `multipart/form-data` requests. Inertia detects a
`File`/`Blob` inside the form data and switches the request body to `FormData`;
CSRF, progress events and the redirect/validation contract keep working exactly
as with a JSON form.

## Server side

::: code-group

```java [Spring Boot]
@PostMapping(value = "/contacts/{id}/avatar",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Object avatar(@PathVariable long id,
        @RequestPart("avatar") MultipartFile avatar) throws IOException {

    if (avatar.isEmpty()) {
        return inertia.back().withErrors(Map.of("avatar", "Choose a file."));
    }
    if (avatar.getSize() > 2 * 1024 * 1024) {
        return inertia.back().withErrors(Map.of("avatar", "Max size is 2 MB."));
    }

    storage.save(id, avatar.getInputStream(), avatar.getContentType());
    return inertia.redirect("/contacts/" + id)
        .withFlash("success", "Avatar updated.");
}
```

```java [Quarkus]
@POST
@Path("/contacts/{id}/avatar")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Uni<Object> avatar(@PathParam("id") long id,
        @RestForm("avatar") FileUpload avatar) {

    if (avatar == null || avatar.size() == 0) {
        return inertia.back().withErrors(Map.of("avatar", "Choose a file."));
    }
    if (avatar.size() > 2 * 1024 * 1024) {
        return inertia.back().withErrors(Map.of("avatar", "Max size is 2 MB."));
    }

    storage.save(id, avatar.uploadedFile());
    return inertia.redirect("/contacts/" + id)
        .withFlash("success", "Avatar updated.");
}
```

:::

The field name (`avatar`) must match the client input name. Both adapters read
the session cookie and CSRF token from the multipart request, so no special
client configuration is required.

## Client side with `useForm`

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { useForm } from '@inertiajs/vue3'

const form = useForm({ avatar: null as File | null })

function submit() {
  form.post('/contacts/1/avatar', {
    preserveScroll: true,
    onSuccess: () => form.reset('avatar'),
  })
}
</script>

<template>
  <form @submit.prevent="submit">
    <input
      type="file"
      accept="image/*"
      @change="form.avatar = ($event.target as HTMLInputElement).files?.[0] ?? null"
    />
    <p v-if="form.errors.avatar" class="error">{{ form.errors.avatar }}</p>
    <progress v-if="form.progress" :value="form.progress.percentage" max="100" />
    <button type="submit" :disabled="form.processing">Upload</button>
  </form>
</template>
```

```tsx [React 19]
import { useForm } from '@inertiajs/react'

export default function AvatarUpload() {
  const { data, setData, post, progress, errors, processing } = useForm<{
    avatar: File | null
  }>({ avatar: null })

  function submit(event: React.FormEvent) {
    event.preventDefault()
    post('/contacts/1/avatar', { preserveScroll: true })
  }

  return (
    <form onSubmit={submit}>
      <input
        type="file"
        accept="image/*"
        onChange={(e) => setData('avatar', e.target.files?.[0] ?? null)}
      />
      {errors.avatar && <p className="error">{errors.avatar}</p>}
      {progress && <progress value={progress.percentage} max={100} />}
      <button type="submit" disabled={processing}>Upload</button>
    </form>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  import { useForm } from '@inertiajs/svelte'

  const form = useForm({ avatar: null as File | null })

  function submit(event: SubmitEvent) {
    event.preventDefault()
    form.post('/contacts/1/avatar', { preserveScroll: true })
  }
</script>

<form onsubmit={submit}>
  <input
    type="file"
    accept="image/*"
    onchange={(e) => (form.avatar = e.currentTarget.files?.[0] ?? null)}
  />
  {#if form.errors.avatar}<p class="error">{form.errors.avatar}</p>{/if}
  {#if form.progress}
    <progress value={form.progress.percentage} max="100"></progress>
  {/if}
  <button type="submit" disabled={form.processing}>Upload</button>
</form>
```

:::

`form.progress` is `null` until the upload starts, then exposes `percentage`,
`loaded` and `total`. Progress is only meaningful for multipart requests.

## Plain `router.post`

When you do not need form state, pass a `FormData` instance or a `File` and let
the client convert it:

```js
import { router } from '@inertiajs/vue3'

const data = new FormData()
data.append('avatar', file)

router.post('/contacts/1/avatar', data, {
  forceFormData: true,
  preserveScroll: true,
})
```

`forceFormData: true` is useful when the payload contains no `File` but the
server still expects multipart (for example a stream or a generated blob).

## Multiple files

```ts
const form = useForm<{ documents: File[] }>({ documents: [] })
form.post('/documents')
```

Spring:

```java
@PostMapping(value = "/documents", consumes = MULTIPART_FORM_DATA_VALUE)
public Object upload(@RequestPart("documents") List<MultipartFile> documents) {
    documents.forEach(storage::save);
    return inertia.redirect("/documents").withFlash("success", "Uploaded.");
}
```

Quarkus:

```java
@POST
@Consumes(MediaType.MULTIPART_FORM_DATA)
public Uni<Object> upload(@RestForm("documents") List<FileUpload> documents) {
    documents.forEach(upload -> storage.save(upload.uploadedFile()));
    return inertia.redirect("/documents").withFlash("success", "Uploaded.");
}
```

## Validation

Upload-specific rules belong on the server:

| Rule | Spring | Quarkus |
|---|---|---|
| Empty file | `MultipartFile.isEmpty()` | `file.size() == 0` |
| Max size | `getSize()` | `size()` |
| Content type | `getContentType()` | `contentType()` |
| Original name | `getOriginalFilename()` | `fileName()` |

Return `inertia.back().withErrors(Map.of("avatar", "..."))`; the client renders
the message next to the input. Never trust the browser `accept` attribute as a
security control.

Framework-level multipart limits still apply:
`spring.servlet.multipart.max-file-size` and
`quarkus.http.body-handler` / `quarkus.http.limits.max-body-size`.

## Security notes

- Store uploads outside the classpath and serve them through a controller or a
  dedicated object store, not from the static assets directory.
- Generate a new file name; never use the client-provided name as a path.
- Validate the real content type, not only the extension.
- Keep `inertia.max-page-bytes` in mind: a page listing thousands of uploads can
  exceed the limit and answer `413`.
- Uploads are mutating requests: CSRF applies exactly like a JSON form.

## Testing uploads

```java
// Spring MockMvc
var file = new MockMultipartFile("avatar", "avatar.png", "image/png", bytes);
mvc.perform(multipart("/contacts/1/avatar").file(file))
    .andExpect(status().isSeeOther())
    .andExpect(redirectedUrl("/contacts/1"));
```

```java
// Quarkus RestAssured
given()
    .multiPart("avatar", new File("src/test/resources/avatar.png"))
    .redirects().follow(false)
    .when().post("/contacts/1/avatar")
    .then().statusCode(303);
```

The TCK suite `10-uploads.yaml` covers multipart requests on Spring MVC,
Quarkus REST and Quarkus Reactive.

## Checklist

- [ ] Input `name` matches `@RequestPart` / `@RestForm`.
- [ ] Server enforces size and content type.
- [ ] Errors are returned with `withErrors` and a stable field key.
- [ ] Progress is rendered from `form.progress`.
- [ ] Uploads are stored outside the public classpath.
- [ ] Multipart limits are configured in the framework.

Next: [Validation](/guide/validation) · [Forms](/guide/forms) ·
[Security](/guide/security)
