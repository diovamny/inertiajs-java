package io.github.diovamny.spring.inertia.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.context.annotation.RequestScope;

import io.github.diovamny.spring.inertia.api.ProvidesInertiaProperties;
import io.github.diovamny.spring.inertia.model.AlwaysProp;

/**
 * Request-scoped registry of shared and "always" props.
 *
 * <p>Shared props ({@code inertia.share(key, value)}) are injected into
 * every page object; "always" props ({@code inertia.always(key, value)})
 * are additionally immune to partial reloads and stay in the page after the
 * shared props are flushed.</p>
 */
@RequestScope
public class SharedDataRegistry {

    private final Map<String, Object> sharedProps = new LinkedHashMap<>();
    private final List<ProvidesInertiaProperties> sharedProviders = new ArrayList<>();
    private final Map<String, AlwaysProp<?>> alwaysProps = new LinkedHashMap<>();

    /**
     * Set a shared prop.
     *
     * @param key   the prop key
     * @param value the value
     */
    public void setSharedProp(String key, Object value) {
        sharedProps.put(key, value);
    }

    /**
     * Set many shared props at once.
     *
     * @param values the entries
     */
    public void setSharedProps(Map<String, Object> values) {
        if (values != null) {
            sharedProps.putAll(values);
        }
    }

    /**
     * Read a shared prop.
     *
     * @param key the prop key
     * @return the value or {@code null}
     */
    public Object sharedProp(String key) {
        return sharedProps.get(key);
    }

    /**
     * Read a shared prop with a fallback default.
     *
     * @param key          the prop key
     * @param defaultValue the default value if absent
     * @return the value or {@code defaultValue}
     */
    public Object sharedProp(String key, Object defaultValue) {
        return sharedProps.getOrDefault(key, defaultValue);
    }

    /**
     * Register a dynamic property provider for this request.
     *
     * @param provider the property provider
     */
    public void addSharedProvider(ProvidesInertiaProperties provider) {
        if (provider != null) {
            sharedProviders.add(provider);
        }
    }

    /**
     * All dynamic property providers registered for this request.
     *
     * @return the list of providers
     */
    public List<ProvidesInertiaProperties> sharedProviders() {
        return Collections.unmodifiableList(sharedProviders);
    }

    /**
     * Flush all standard shared props and providers for the current request.
     * "Always" props are preserved.
     */
    public void flushShared() {
        sharedProps.clear();
        sharedProviders.clear();
    }

    /**
     * All shared props registered for this request.
     *
     * @return the entries
     */
    public Map<String, Object> sharedProps() {
        return sharedProps;
    }

    /**
     * Register an "always" prop.
     *
     * @param key   the prop key
     * @param value the wrapped value
     */
    public void setAlwaysProp(String key, AlwaysProp<?> value) {
        alwaysProps.put(key, value);
    }

    /**
     * Register many "always" props at once.
     *
     * @param values the entries
     */
    public void setAlwaysProps(Map<String, AlwaysProp<?>> values) {
        if (values != null) {
            alwaysProps.putAll(values);
        }
    }

    /**
     * All "always" props registered for this request.
     *
     * @return the entries
     */
    public Map<String, AlwaysProp<?>> alwaysProps() {
        return alwaysProps;
    }
}
