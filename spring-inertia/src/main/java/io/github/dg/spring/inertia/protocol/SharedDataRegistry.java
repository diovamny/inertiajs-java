package io.github.dg.spring.inertia.protocol;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.context.annotation.RequestScope;

import io.github.dg.spring.inertia.model.AlwaysProp;

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
        sharedProps.putAll(values);
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
        alwaysProps.putAll(values);
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