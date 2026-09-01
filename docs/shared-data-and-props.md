# Shared Data and Props Guide — Inertia.js Java Adapters

This guide covers how to share data globally across all Inertia responses, and the various prop evaluation strategies available (lazy, always, once, deferred, merge, deep-merge, prepend, optional).

---

## Table of Contents

1. [Shared Data](#shared-data)
   - [InertiaSharedDataContributor SPI](#inertiashareddatacontributor-spi)
   - [Programmatic sharing](#programmatic-sharing)
2. [Prop Strategies](#prop-strategies)
   - [Always props](#always-props)
   - [Once props](#once-props)
   - [Lazy / Deferred props](#lazy--deferred-props)
   - [Merge props](#merge-props)
   - [Deep merge props](#deep-merge-props)
   - [Prepend props](#prepend-props)
   - [Optional props](#optional-props)
3. [Partial Reloads](#partial-reloads)

---

## Shared Data

### InertiaSharedDataContributor SPI

The recommended way to contribute global shared data is via the `InertiaSharedDataContributor` SPI. Any bean implementing this interface will be automatically discovered and invoked before every render.

#### Spring Boot

```java
import io.github.dg.spring.inertia.spi.InertiaSharedDataContributor;
import io.github.dg.spring.inertia.Inertia;
import org.springframework.stereotype.Component;

@Component
public class AuthSharedData implements InertiaSharedDataContributor {
    private final UserService userService;

    public AuthSharedData(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void contribute(Inertia inertia) {
        inertia.share("auth", Map.of(
            "user", userService.currentUser(),
            "permissions", userService.currentPermissions()
        ));
        inertia.share("flash", Map.of(
            "success", getFlashSuccess(),
            "error", getFlashError()
        ));
    }
}
```

#### Quarkus

```java
import io.github.dg.quarkus.inertia.spi.InertiaSharedDataContributor;
import io.github.dg.quarkus.inertia.api.Inertia;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthSharedData implements InertiaSharedDataContributor {
    @Inject UserService userService;

    @Override
    public void contribute(Inertia inertia) {
        inertia.share("auth", Map.of(
            "user", userService.currentUser()
        ));
    }
}
```

> **Note**: Multiple contributors are supported. They are invoked in registration order (Spring) or CDI resolution order (Quarkus).

### Programmatic Sharing

You can also share data directly in a controller or filter:

```java
// Spring — inject Inertia bean
@GetMapping("/dashboard")
public ResponseEntity<?> dashboard(Inertia inertia) {
    inertia.share("notifications", getNotifications());
    return inertia.render("Dashboard", Map.of("user", currentUser()));
}

// Quarkus — inject Inertia CDI bean
@Inject Inertia inertia;

@GET @Path("/dashboard")
public Response dashboard() {
    inertia.share("notifications", getNotifications());
    return inertia.render("Dashboard", Map.of("user", currentUser()));
}
```

#### Shared Data API

| Method | Description |
|--------|-------------|
| `share(key, value)` | Shares a single key/value for the current request |
| `share(key, supplier)` | Shares a lazily evaluated value |
| `share(Map)` | Shares multiple key/values at once |
| `shared(key, defaultValue)` | Retrieves a previously shared value with a default |
| `flushShared()` | Clears all shared data for the current request |
| `always(key, value)` | Adds a value included on every response including partial reloads |

---

## Prop Strategies

### Always Props

Always-props are included in **every** response, including partial reloads, even if not listed in `X-Inertia-Partial-Data`.

```java
// Spring
public ResponseEntity<?> page(Inertia inertia) {
    return inertia.render("Page", props -> props
        .always("auth", authData())     // always included
        .with("feed", feedData())       // excluded on partial reload unless listed
    );
}

// Quarkus
return inertia.render("Page", props -> props
    .always("auth", authData())
    .with("feed", feedData())
);
```

### Once Props

Once-props are included only on the **first** full page visit, and excluded on subsequent partial reloads.

```java
props.once("announcement", () -> getAnnouncement())
```

### Lazy / Deferred Props

Lazy props are resolved only when their key is explicitly listed in `X-Inertia-Partial-Data`. They are perfect for expensive computations:

```java
// Spring
props.defer("expensiveReport", () -> reportService.generate())

// Quarkus
props.defer("expensiveReport", () -> reportService.generate())
```

The client initiates a partial reload to fetch deferred props after the initial page has rendered.

### Merge Props

Merge-props shallow-merge new data with existing client-side state instead of replacing it:

```java
props.merge("feed", () -> getFeedPage(2))
```

### Deep Merge Props

Deep-merge-props recursively merge nested maps:

```java
props.deepMerge("settings", () -> getUserSettings())
```

### Prepend Props

Prepend-props add new items to the beginning of a list:

```java
props.prepend("notifications", () -> getNewNotifications())
```

### Optional Props

Optional props are only evaluated and included if explicitly requested. Unlike deferred props they don't send a deferred group indicator:

```java
props.optional("sidebarData", () -> getSidebarData())
```

---

## Partial Reloads

Partial reloads allow the frontend to refresh only specific props. The adapter handles this automatically via the `X-Inertia-Partial-Data` and `X-Inertia-Partial-Component` headers.

**Request headers set by the Inertia client:**

| Header | Description |
|--------|-------------|
| `X-Inertia-Partial-Component` | The component currently loaded on the client |
| `X-Inertia-Partial-Data` | Comma-separated list of prop keys to include |
| `X-Inertia-Partial-Except` | Comma-separated list of prop keys to exclude |
| `X-Inertia-Except-Once-Props` | Props that should not be treated as once-props |
| `X-Inertia-Reset` | Forces a full reset of specified props |

The adapters fully support all of the above headers out of the box.
