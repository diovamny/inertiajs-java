# Scroll Management

Inertia restores the scroll position on browser back/forward navigation and
resets it on a normal visit. You can override that behavior per link, per form
or per programmatic visit, and mark scrollable containers so their position is
remembered too.

## Default behavior

| Navigation | Scroll behavior |
|---|---|
| `<Link>` to a new page | Scrolls to the top |
| Browser back/forward | Restores the saved position |
| Partial reload (`router.reload`) | Keeps the current position |
| Form submit that redirects | Follows the new page top |
| Visit with `preserveScroll: true` | Keeps the current position |

## `preserveScroll`

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { Link, router } from '@inertiajs/vue3'
</script>

<template>
  <Link href="/contacts" preserve-scroll>Refresh list</Link>

  <button @click="router.reload({ only: ['contacts'], preserveScroll: true })">
    Reload contacts
  </button>
</template>
```

```tsx [React 19]
import { Link, router } from '@inertiajs/react'

<Link href="/contacts" preserveScroll>Refresh list</Link>

<button
  onClick={() => router.reload({ only: ['contacts'], preserveScroll: true })}
>
  Reload contacts
</button>
```

```svelte [Svelte 5]
<script lang="ts">
  import { Link, router } from '@inertiajs/svelte'
</script>

<Link href="/contacts" preserveScroll>Refresh list</Link>

<button
  onclick={() => router.reload({ only: ['contacts'], preserveScroll: true })}
>
  Reload contacts
</button>
```

:::

`preserveScroll` accepts a boolean or a callback:

```js
router.visit('/contacts', {
  preserveScroll: (page) => page.props.filters?.page > 1,
})
```

The callback form is useful when only some visits should keep the position.

## Preserve form state

`preserveState` keeps the component instance alive across a visit, including
local refs, open dialogs and input focus. It pairs naturally with
`preserveScroll`:

```js
router.get('/contacts', { search: 'ada' }, {
  preserveState: true,
  preserveScroll: true,
  only: ['contacts'],
})
```

Use it for filters and search-as-you-type. Do not use it when navigating to a
different resource: the stale component state usually surprises users more than
the saved scroll does.

## Scroll regions

By default Inertia tracks the document scroll position. Add the `scroll-region`
attribute to any scrollable element and its position is saved and restored too:

```html
<div class="sidebar" scroll-region>
  <!-- long navigation tree -->
</div>

<main class="content" scroll-region>
  <!-- long article -->
</main>
```

```vue
<template>
  <aside scroll-region>
    <nav>...</nav>
  </aside>
  <main scroll-region>
    <slot />
  </main>
</template>
```

Only elements with the attribute are tracked. Nested regions are supported; each
one restores its own `scrollTop`/`scrollLeft` on history navigation.

## Infinite scroll

Server side, register the collection as a scroll prop:

```java
@GetMapping("/contacts")
public Object index(@RequestParam(defaultValue = "1") int page) {
    var result = contacts.page(page, 25);
    inertia.scroll("contacts", result.content(), Map.of(
        "currentPage", result.number(),
        "previousPage", page > 1 ? page - 1 : null,
        "nextPage", result.hasNext() ? page + 1 : null,
        "pageName", "page",
        "matchOn", "id"
    ));
    return inertia.render("Contacts/Index", Map.of("filters", filters.current()));
}
```

Client side, wrap the list with `InfiniteScroll`:

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { InfiniteScroll } from '@inertiajs/vue3'

defineProps<{ contacts: Contact[] }>()
</script>

<template>
  <InfiniteScroll data="contacts" only-next>
    <div v-for="contact in contacts" :key="contact.id">
      {{ contact.name }}
    </div>
  </InfiniteScroll>
</template>
```

```tsx [React 19]
import { InfiniteScroll } from '@inertiajs/react'

<InfiniteScroll data="contacts" onlyNext>
  {contacts.map((contact) => (
    <div key={contact.id}>{contact.name}</div>
  ))}
</InfiniteScroll>
```

```svelte [Svelte 5]
<script lang="ts">
  import { InfiniteScroll } from '@inertiajs/svelte'
  let { contacts } = $props()
</script>

<InfiniteScroll data="contacts" onlyNext>
  {#each contacts as contact (contact.id)}
    <div>{contact.name}</div>
  {/each}
</InfiniteScroll>
```

:::

The component performs partial reloads with the next `page` query parameter and
the `X-Inertia-Infinite-Scroll-Merge-Intent` header (`append` or `prepend`). The
adapter merges the new page into the existing prop using `matchOn` and the
`mergeProps` metadata, so no manual concatenation is needed.

### Manual pagination

If you prefer buttons, keep the same server metadata and call the router
yourself:

```js
router.get('/contacts', { page: nextPage }, {
  only: ['contacts'],
  preserveScroll: true,
  preserveState: true,
})
```

The response carries `mergeProps: ['contacts']` because the prop was registered
with `inertia.scroll(...)`, so the client merges instead of replacing.

## Reset a collection

Changing a filter should start from page one. Send `reset` so the merge state is
discarded:

```js
router.get('/contacts', { search: term, page: 1 }, {
  only: ['contacts'],
  reset: ['contacts'],
  preserveState: true,
  preserveScroll: true,
})
```

Without `reset`, the client would append the new filtered page to the previous
collection.

## Back navigation

Inertia saves the document position and every `scroll-region` in the history
state. On `popstate` it restores them after the page component is mounted. Two
practical rules:

- Render the same list height on the way back, or the restore will clamp to the
  new maximum.
- If a page loads data asynchronously above the saved position, restore after
  that data renders. A deferred prop that expands the page can push the content
  down; use `preserveScroll` on the reload that fills it.

## Testing scroll behavior

The Playwright contracts cover the real browser behavior. A minimal check:

```ts
await page.goto('/contacts')
await page.mouse.wheel(0, 1200)
const before = await page.evaluate(() => window.scrollY)

await page.getByRole('link', { name: 'Contact 20' }).click()
await page.goBack()

await expect
  .poll(() => page.evaluate(() => window.scrollY))
  .toBeGreaterThan(before - 50)
```

For scroll regions assert `element.scrollTop` rather than `window.scrollY`.

## Checklist

- [ ] `preserveScroll` is used for filters, polls and partial reloads.
- [ ] `preserveState` is used only when component state should survive.
- [ ] Long independent panes have the `scroll-region` attribute.
- [ ] Infinite lists use `inertia.scroll` metadata and `matchOn`.
- [ ] Filter changes send `reset` for the collection.
- [ ] Back navigation is tested with real content height.

Next: [History](/guide/history) · [Partial Reloads](/guide/partial-reloads) ·
[Asset Versioning](/guide/asset-versioning)
