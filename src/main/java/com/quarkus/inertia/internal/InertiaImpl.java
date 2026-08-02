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
        sharedData.addDeferredPropGroup(group, List.of(name));
        sharedData.set(name, resolver);
    }

    @Override
    public void once(String key, Object value) {
        oncePropRegistry.set(key, value);
        sharedData.addOncePropKey(key, key);
    }

    @Override
    public void merge(String key, Object value) {
        sharedData.addMergePropKey(key);
        sharedData.set(key, value);
    }

    @Override
    public <T> void once(String key, T value, String customKey) {
        oncePropRegistry.set(key, value, customKey, null);
        sharedData.addOncePropKey(key, customKey);
    }

    @Override
    public <T> void merge(String key, T value, boolean deep) {
        sharedData.addMergePropKey(key);
        if (deep) {
            @SuppressWarnings("unchecked")
            var existing = (Map<String, Object>) sharedData.get(key);
            if (existing != null && value instanceof Map) {
                var merged = mergePropProcessor.merge(
                    existing, (Map<String, Object>) value);
                sharedData.set(key, merged);
            } else {
                sharedData.set(key, value);
            }
        } else {
            sharedData.set(key, value);
        }
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
}
