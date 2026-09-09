package io.github.diovamny.quarkus.inertia.api;

/**
 * The unified Inertia server-side entry point for Quarkus, modeled after the official
 * {@code inertia-laravel} / {@code inertia-rails} adapters.
 *
 * <p>Inject it into any CDI/JAX-RS component or Vert.x route handler. The request/response
 * handling (JSON page vs. HTML, redirects, partial reloads, deferred props, validation errors)
 * is resolved automatically based on the {@code X-Inertia} headers.</p>
 *
 * <p>Basic usage:</p>
 * <pre>{@code
 * @Path("/contacts")
 * public class ContactsController {
 *
 *     @Inject
 *     Inertia inertia;
 *
 *     @GET
 *     public Uni<Object> index() {
 *         return inertia.render("Contacts/Index",
 *             Map.of("contacts", contactRepository.listAll()));
 *     }
 *
 *     @PUT
 *     @Path("{id}")
 *     @Consumes(MediaType.APPLICATION_JSON)
 *     public Uni<Object> update(@PathParam("id") long id, ContactForm form) {
 *         contacts.update(id, form);
 *         return inertia.back().with("success", "Contact updated.");
 *     }
 * }
 * }</pre>
 *
 * <h2>Interface Segregation Principle (ISP)</h2>
 * <p>This interface is a composite facade inheriting from dedicated role interfaces:</p>
 * <ul>
 *   <li>{@link InertiaRenderer}: reactive, synchronous, and Vert.x rendering methods</li>
 *   <li>{@link InertiaRedirector}: internal/external redirects, back navigation, and location responses</li>
 *   <li>{@link InertiaProps}: shared data, deferred, once, cached, merge, and scroll props</li>
 *   <li>{@link InertiaFlash}: session flash data for messages and errors</li>
 *   <li>{@link InertiaMetadata}: viewData, page metadata, history encryption/clearing, SSR, and versioning</li>
 * </ul>
 * <p>Consumers may inject {@link Inertia} directly or inject any of the sub-interfaces individually
 * when only a specific subset of capabilities is required.</p>
 */
public interface Inertia extends
        InertiaRenderer,
        InertiaRedirector,
        InertiaProps,
        InertiaFlash,
        InertiaMetadata {
}
