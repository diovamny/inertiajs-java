package io.github.dg.quarkus.inertia.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * The Inertia "page object": the JSON payload exchanged with the frontend.
 *
 * <p>Serialized by the JSON provider and served for every Inertia request.
 * Contains the component name, the props, the resolved URL and the asset
 * version, plus optional metadata (deferred/merge/once/scroll props, history
 * instructions, etc.). {@code null} metadata is omitted from the JSON
 * payload.</p>
 *
 * @param component        the frontend component name
 * @param props            the page props
 * @param url              the resolved request URL
 * @param version          the asset version used for cache-busting
 * @param flash            one-time flash data delivered to the client
 *                         ({@code page.flash}), {@code null} when empty
 * @param deferredProps    deferred groups ({@code group -> member names})
 * @param mergeProps       mergeable prop names
 * @param prependProps     prependable prop names
 * @param deepMergeProps   props merged with deep semantics
 * @param matchPropsOn     dot-notation fields used to match list elements
 * @param onceProps        once-props metadata ({@code key -> OnceProp})
 * @param scrollProps      scroll prop metadata ({@code key -> metadata})
 * @param sharedProps      names of the shared props
 * @param rescuedProps     names of the props preserved in client history
 * @param meta             extra page metadata
 * @param encryptHistory   whether the client must encrypt history state
 * @param clearHistory     whether the client must clear its history
 * @param preserveFragment whether the client must preserve the URL fragment
 */
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageObject(
    String component,
    Map<String, Object> props,
    String url,
    String version,
    Map<String, Object> flash,
    Map<String, List<String>> deferredProps,
    List<String> mergeProps,
    List<String> prependProps,
    List<String> deepMergeProps,
    List<String> matchPropsOn,
    Map<String, OnceProp> onceProps,
    Map<String, Map<String, Object>> scrollProps,
    List<String> sharedProps,
    List<String> rescuedProps,
    Map<String, Object> meta,
    Boolean encryptHistory,
    Boolean clearHistory,
    Boolean preserveFragment
) {
    /**
     * Compact constructor validating the component name and normalizing a
     * {@code null} props map to an empty one.
     *
     * @throws IllegalArgumentException if {@code component} is {@code null}
     *                                  or blank
     */
    public PageObject {
        if (component == null || component.isBlank()) {
            throw new IllegalArgumentException("component must not be blank");
        }
        if (props == null) props = Map.of();
    }

    /**
     * Convenience constructor for a page object without metadata.
     *
     * @param component the frontend component name
     * @param props     the page props (may be {@code null})
     * @param url       the current request URL
     * @param version   the asset version
     */
    public PageObject(
            String component,
            Map<String, Object> props,
            String url,
            String version) {
        this(component, props, url, version,
            null, null, null, null, null, null, null, null, null, null, null,
            false, false, false);
    }

    /**
     * Return a copy of this page object with the given props.
     *
     * @param newProps the replacement props
     * @return a new page object with identical metadata
     */
    public PageObject withProps(Map<String, Object> newProps) {
        return new PageObject(component, newProps, url, version,
            flash, deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    /**
     * Return a copy of this page object with the given merge metadata.
     *
     * @param newMergeProps     the mergeable prop names
     * @param newPrependProps   the prependable prop names
     * @param newDeepMergeProps the deep-merge prop names
     * @param newMatchPropsOn   the match-on fields (dot notation)
     * @return a new page object with identical props and remaining metadata
     */
    public PageObject withMergeMetadata(List<String> newMergeProps, List<String> newPrependProps,
            List<String> newDeepMergeProps, List<String> newMatchPropsOn) {
        return new PageObject(component, props, url, version,
            flash, deferredProps, newMergeProps, newPrependProps, newDeepMergeProps, newMatchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    /**
     * Return a copy of this page object with a different version.
     *
     * @param newVersion the replacement asset version
     * @return a new page object
     */
    public PageObject withVersion(String newVersion) {
        return new PageObject(component, props, url, newVersion,
            flash, deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    /**
     * Return a copy of this page object with a different URL.
     *
     * @param newUrl the replacement URL
     * @return a new page object
     */
    public PageObject withUrl(String newUrl) {
        return new PageObject(component, props, newUrl, version,
            flash, deferredProps, mergeProps, prependProps, deepMergeProps, matchPropsOn, onceProps,
            scrollProps, sharedProps, rescuedProps, meta,
            encryptHistory, clearHistory, preserveFragment);
    }

    /**
     * Whether the page object carries any metadata beyond the core fields.
     *
     * @return {@code true} when at least one metadata section is present
     */
    public boolean hasMetadata() {
        return (deferredProps != null && !deferredProps.isEmpty()) ||
               (mergeProps != null && !mergeProps.isEmpty()) ||
               (prependProps != null && !prependProps.isEmpty()) ||
               (deepMergeProps != null && !deepMergeProps.isEmpty()) ||
               (matchPropsOn != null && !matchPropsOn.isEmpty()) ||
               (onceProps != null && !onceProps.isEmpty()) ||
               (scrollProps != null && !scrollProps.isEmpty()) ||
               (sharedProps != null && !sharedProps.isEmpty()) ||
               (rescuedProps != null && !rescuedProps.isEmpty()) ||
               (meta != null && !meta.isEmpty()) ||
               Boolean.TRUE.equals(encryptHistory) ||
               Boolean.TRUE.equals(clearHistory) ||
               Boolean.TRUE.equals(preserveFragment);
    }

    /**
     * Whether the page object declares any deferred prop groups.
     *
     * @return {@code true} when {@code deferredProps} is non-empty
     */
    public boolean hasDeferredProps() {
        return deferredProps != null && !deferredProps.isEmpty();
    }
}