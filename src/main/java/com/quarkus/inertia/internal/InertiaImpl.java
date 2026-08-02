package com.quarkus.inertia.internal;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.api.Inertia;
import com.quarkus.inertia.config.InertiaConfig;
import com.quarkus.inertia.model.AlwaysProp;
import com.quarkus.inertia.protocol.PageObjectBuilder;
import com.quarkus.inertia.protocol.ResponseProcessor;
import com.quarkus.inertia.protocol.SharedDataRegistry;
import com.quarkus.inertia.protocol.RedirectProcessor;
import com.quarkus.inertia.protocol.OncePropRegistry;
import com.quarkus.inertia.protocol.MergePropProcessor;
import com.quarkus.inertia.spi.FlashStore;
import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class InertiaImpl implements Inertia {

    private final PageObjectBuilder pageBuilder;
    private final ResponseProcessor responseProcessor;
    private final SharedDataRegistry sharedData;
    private final RedirectProcessor redirectProcessor;
    private final VersionProvider versionProvider;
    private final OncePropRegistry oncePropRegistry;
    private final MergePropProcessor mergePropProcessor;
    private final FlashStore flashStore;
    private final InertiaConfig config;

    @Inject
    public InertiaImpl(
            PageObjectBuilder pageBuilder,
            ResponseProcessor responseProcessor,
            SharedDataRegistry sharedData,
            RedirectProcessor redirectProcessor,
            VersionProvider versionProvider,
            OncePropRegistry oncePropRegistry,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            InertiaConfig config) {
        this.pageBuilder = pageBuilder;
        this.responseProcessor = responseProcessor;
        this.sharedData = sharedData;
        this.redirectProcessor = redirectProcessor;
        this.versionProvider = versionProvider;
        this.oncePropRegistry = oncePropRegistry;
        this.mergePropProcessor = mergePropProcessor;
        this.flashStore = flashStore;
        this.config = config;
    }

    @Override
    public Uni<Object> render(String component, Map<String, Object> props) {
        return pageBuilder.build(component, props)
            .chain(responseProcessor::process);
    }

    @Override
    public Uni<Object> render(String component) {
        return render(component, Map.of());
    }

    @Override
    public Uni<Object> redirect(String url) {
        return redirectProcessor.process(url);
    }

    @Override
    public Uni<Object> back() {
        return redirectProcessor.back();
    }

    @Override
    public Uni<Object> back(String fallback) {
        return redirectProcessor.back(fallback);
    }

    @Override
    public Uni<Object> back(int status, String fallback) {
        return redirectProcessor.back(status, fallback);
    }

    @Override
    public Uni<Object> location(String url) {
        return redirectProcessor.external(url);
    }

    @Override
    public void share(String key, Object value) {
        sharedData.set(key, value);
    }

    @Override
    public void share(Map<String, Object> values) {
        sharedData.setAll(values);
    }

    @Override
    public void always(String key, Object value) {
        sharedData.set(key, AlwaysProp.of(value));
    }

    @Override
    public void flash(String key, Object value) {
        flashStore.put(key, value);
    }

    @Override
    public void flash(Map<String, Object> values) {
        flashStore.putAll(values);
    }

    @Override
    public void deferred(String group, String name, Supplier<Uni<Object>> resolver) {
        var keys = sharedData.getDeferredPropGroups().getOrDefault(group, java.util.List.of());
        var updated = new java.util.ArrayList<>(keys);
        if (!updated.contains(name)) updated.add(name);
        sharedData.addDeferredPropGroup(group, updated);
        sharedData.setWithNoTrack(name, resolver);
    }

    @Override
    public void deferred(String name, Supplier<Uni<Object>> resolver) {
        deferred("default", name, resolver);
    }

    @Override
    public void optional(String key, Supplier<Uni<Object>> resolver) {
        sharedData.addOptionalProp(key, resolver);
    }

    @Override
    public void once(String key, Object value) {
        oncePropRegistry.set(key, value);
    }

    @Override
    public void once(String key, Object value, String customKey) {
        oncePropRegistry.set(key, value, customKey, null);
    }

    @Override
    public void merge(String key, Object value) {
        sharedData.merge(key, value, false);
    }

    @Override
    public void merge(String key, Object value, boolean deep) {
        sharedData.merge(key, value, deep);
    }

    @Override
    public void merge(String key, Object value, boolean deep, String... matchOn) {
        sharedData.merge(key, value, deep, matchOn);
    }

    @Override
    public void prepend(String key, Object value) {
        sharedData.addMergePropKey(key);
        sharedData.addPrependPropKey(key);
        sharedData.set(key, value);
    }

    @Override
    public void scroll(String key, Map<String, Object> metadata) {
        sharedData.addScrollProp(key, metadata);
    }

    @Override
    public void rescue(String key) {
        sharedData.addRescuedProp(key);
    }

    @Override
    public void meta(String key, Object value) {
        sharedData.addMeta(key, value);
    }

    @Override
    public void meta(Map<String, Object> values) {
        sharedData.addMeta(values);
    }

    @Override
    public void encryptHistory(boolean encrypt) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-encrypt-history", encrypt);
        }
    }

    @Override
    public void clearHistory(boolean clear) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-clear-history", clear);
        }
    }

    @Override
    public void preserveFragment(boolean preserve) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-preserve-fragment", preserve);
        }
    }

    boolean isEncryptHistory() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-encrypt-history");
            if (val != null) return (Boolean) val;
        }
        return config.encryptHistory();
    }

    boolean isClearHistory() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-clear-history");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    boolean isPreserveFragment() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            var val = ctx.getLocal("inertia-preserve-fragment");
            return Boolean.TRUE.equals(val);
        }
        return false;
    }

    @Override
    public String getVersion() {
        return versionProvider.getVersion();
    }

    @Override
    public void version(String version) {
        versionProvider.setVersion(version);
    }

    @Override
    public void setRootView(String name) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-root-view", name);
        }
    }

    @Override
    public void withoutSsr(String... paths) {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-ssr-exclude-paths", java.util.List.of(paths));
        }
    }

    @Override
    public void disableSsr() {
        var ctx = io.vertx.core.Vertx.currentContext();
        if (ctx != null) {
            ctx.putLocal("inertia-disable-ssr", Boolean.TRUE);
        }
    }

    @Override
    public Map<String, Object> getShared() {
        return sharedData.getShared();
    }

    @Override
    public Object getShared(String key, Object defaultValue) {
        return sharedData.getShared(key, defaultValue);
    }

    @Override
    public void flushShared() {
        sharedData.flushShared();
    }
}
