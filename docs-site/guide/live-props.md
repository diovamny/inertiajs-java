# Live Props

Live props are resolved by the server like any other prop, but their page
metadata tells the official client which broadcast channel and event should
refresh them. The value is real data, not a subscription token: the resolver
runs on the initial visit and again whenever a selected partial reload asks
for it.

Live props were introduced in **0.0.6** and require `@inertiajs/*` **3.11.0**
on the client.

## Declaring a live prop

::: code-group

```java [Spring Boot]
import io.github.diovamny.inertia.core.model.LiveChannel;
import io.github.diovamny.inertia.core.model.LivePropListener;

@GetMapping("/inventory")
public Object index() {
    inertia.live("stockCount", stock::current, 2000,
        new LivePropListener(
            LiveChannel.privateChannel("inventory"),
            "StockUpdated"
        ));

    return inertia.render("Inventory/Index", Map.of(
        "channelName", "inventory",
        "channelType", "private"
    ));
}
```

```java [Quarkus]
import io.github.diovamny.inertia.core.model.LiveChannel;
import io.github.diovamny.inertia.core.model.LivePropListener;

@GET
@Path("/inventory")
public Uni<Object> index() {
    inertia.live("stockCount",
        () -> Uni.createFrom().item(stock.current()), 2000,
        new LivePropListener(
            LiveChannel.privateChannel("inventory"),
            "StockUpdated"
        ));

    return inertia.render("Inventory/Index", Map.of(
        "channelName", "inventory",
        "channelType", "private"
    ));
}
```

:::

| Parameter | Meaning |
|---|---|
| `key` | Prop name exposed to the component |
| `resolver` | Spring `Supplier<Object>`; Quarkus `Supplier<Uni<Object>>` |
| `throttleMs` | Minimum interval between refresh requests |
| `listeners` | Channels and event names that trigger an update |

There are two overloads: `live(key, resolver, listeners...)` and
`live(key, resolver, throttleMs, listeners...)`. The throttle is optional and
is omitted from the wire when not set.

## Channels

`LiveChannel` has three factories, matching the official broadcasting
visibility model:

| Factory | Wire `type` | Use |
|---|---|---|
| `LiveChannel.publicChannel("orders")` | `public` | Public dashboards and stock tickers |
| `LiveChannel.privateChannel("orders.1")` | `private` | Per-user or per-tenant data |
| `LiveChannel.presence("room.42")` | `presence` | Collaborative screens with membership |

A live prop may listen to several channels and events:

```java
// Spring (Quarkus: Supplier<Uni<Object>>)
inertia.live("orderStatus", order::status,
    new LivePropListener(LiveChannel.privateChannel("orders.1"), "OrderUpdated", "OrderCancelled"),
    new LivePropListener(LiveChannel.privateChannel("tenant.7"), "Refresh"));
```

The server never opens the connection. It only describes the subscription; the
client uses its configured broadcaster (Pusher, Reverb, Ably, Mercure, …).

## Wire shape

The page object contains the evaluated value in `props` and the metadata in
`liveProps`:

```json
{
  "component": "Inventory/Index",
  "props": {
    "stockCount": 42,
    "channelName": "inventory",
    "channelType": "private"
  },
  "liveProps": {
    "stockCount": {
      "listeners": [
        {
          "channel": { "name": "inventory", "type": "private" },
          "events": ["StockUpdated"]
        }
      ],
      "throttle": 2000
    }
  }
}
```

`liveProps` is omitted entirely when no live prop is registered, so existing
applications keep the exact previous payload. The resolver is never serialized:
only the channel names, types, events and throttle travel.

## Resolution semantics

| Visit | Resolver behavior |
|---|---|
| Full visit | Runs once and the value is included |
| Partial reload selecting the key | Runs and returns the fresh value |
| Partial reload selecting another key | Not called |
| Partial component mismatch | Not called |
| Deferred + live, full visit | Not called until selected |

This is pinned by TCK suite `11-live-props.yaml` and the Spring/Quarkus
integration suites. Foreign keys never execute the resolver, which matters when
the resolver performs a query or calls a remote service.

## Deferred + live

A prop can be both deferred and live. The key appears in `deferredProps` and
`liveProps`, and the value is not resolved on the first visit:

::: code-group

```java [Spring Boot]
return ((InertiaRender) inertia.render("Inventory/Index", Map.of()))
    .defer("slow", "stockCount", stock::current)
    .live("stockCount", stock::current, 2000,
        new LivePropListener(
            LiveChannel.privateChannel("inventory"), "StockUpdated"));
```

```java [Quarkus]
inertia.deferred("slow", "stockCount",
    () -> Uni.createFrom().item(stock.current()));
inertia.live("stockCount",
    () -> Uni.createFrom().item(stock.current()), 2000,
    new LivePropListener(
        LiveChannel.privateChannel("inventory"), "StockUpdated"));

return inertia.render("Inventory/Index", Map.of());
```

:::

The client first loads the deferred group (`only: ['stockCount']`), then keeps
it fresh from the broadcast events. `LivePropsSpringIntegrationTest` and
`LivePropsQuarkusTest` assert the composition on all three transports.

## Client behavior

The official 3.11 client reads `liveProps` and subscribes to the named channels
through the broadcast adapter configured in `createInertiaApp`. When a listed
event fires, it performs a partial reload of exactly that key, respecting the
throttle. No page-specific code is required.

If the application does not configure a broadcaster, the metadata is inert and
the page still renders the server value. You can always refresh manually:

::: code-group

```js [Vue 3]
import { router } from '@inertiajs/vue3'

// Called from a websocket handler, a poll or a button
router.reload({ only: ['stockCount'] })
```

```js [React 19]
import { router } from '@inertiajs/react'

router.reload({ only: ['stockCount'] })
```

```js [Svelte 5]
import { router } from '@inertiajs/svelte'

router.reload({ only: ['stockCount'] })
```

:::

The partial reload response keeps `liveProps.stockCount`, so the subscription
metadata survives every refresh without extra work.

## Component example

::: code-group

```vue [Vue 3]
<script setup lang="ts">
defineProps<{ stockCount: number; channelName: string }>()
</script>

<template>
  <section>
    <h2>Inventory</h2>
    <p data-testid="stock">{{ stockCount }} units left</p>
    <small>Live on {{ channelName }}</small>
  </section>
</template>
```

```tsx [React 19]
export default function Inventory({
  stockCount,
  channelName,
}: {
  stockCount: number
  channelName: string
}) {
  return (
    <section>
      <h2>Inventory</h2>
      <p data-testid="stock">{stockCount} units left</p>
      <small>Live on {channelName}</small>
    </section>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  let { stockCount, channelName } = $props()
</script>

<section>
  <h2>Inventory</h2>
  <p data-testid="stock">{stockCount} units left</p>
  <small>Live on {channelName}</small>
</section>
```

:::

## Testing live props

Assert both the value and the metadata:

```java
// Spring MockMvc
result.andExpect(InertiaResultMatchers.inertia()
    .component("Inventory/Index")
    .prop("stockCount", 42));

// Quarkus RestAssured
InertiaPage.fromJson(response.asString())
    .assertComponent("Inventory/Index")
    .assertProp("stockCount", 42);
```

For the wire metadata, parse the page JSON and check
`liveProps.stockCount.listeners[0].channel.type` and
`liveProps.stockCount.throttle`; that is exactly what the `LP-01..LP-04`
Playwright contracts do against both kitchen sinks.

## Checklist

- [ ] Client pinned to `@inertiajs/*` 3.11.0 or newer.
- [ ] Resolver is cheap, safe to call repeatedly and never returns secrets.
- [ ] Channel visibility matches the data: private for per-user values.
- [ ] Throttle set for high-frequency events.
- [ ] Partial reload test proves foreign keys do not run the resolver.
- [ ] Broadcaster configured on the client, or a manual reload fallback exists.

Next: [Partial Reloads](/guide/partial-reloads) · [Props](/guide/props) ·
[Responses & BigInt](/guide/responses)
