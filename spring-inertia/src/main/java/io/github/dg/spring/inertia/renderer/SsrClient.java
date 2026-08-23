package io.github.dg.spring.inertia.renderer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.spi.JsonProvider;

/**
 * Client for the Inertia SSR server: POSTs the page object and receives the
 * rendered {@code head}/{@code body} fragments.
 */
public class SsrClient {

    private final RestClient restClient;
    private final JsonProvider jsonProvider;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    public SsrClient(InertiaProperties properties, JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.connectTimeout = properties.getSsrConnectTimeout();
        this.readTimeout = properties.getSsrReadTimeout();

        var requestFactory = new SimpleClientHttpRequestFactory();
        if (connectTimeout != null) {
            requestFactory.setConnectTimeout((int) connectTimeout.toMillis());
        }
        if (readTimeout != null) {
            requestFactory.setReadTimeout((int) readTimeout.toMillis());
        }

        this.restClient = RestClient.builder()
            .baseUrl(properties.getSsrUrl())
            .requestFactory(requestFactory)
            .build();
    }

    /**
     * Render a page object server-side.
     *
     * @param page the page object
     * @return the SSR result, or empty when the server is unavailable or times out
     */
    public Optional<SsrResult> render(PageObject page) {
        try {
            var body = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(jsonProvider.toJson(page))
                .retrieve()
                .body(SsrResult.class);
            return Optional.ofNullable(body);
        } catch (Exception e) {
            // Log the error but don't expose details to the client
            return Optional.empty();
        }
    }

    /**
     * Payload returned by the SSR server.
     *
     * @param head HTML fragments for the document head
     * @param body HTML fragments for the app element
     */
    public record SsrResult(List<String> head, String body) {

        /**
         * The head fragments joined into one HTML snippet.
         *
         * @return the joined head
         */
        public String headHtml() {
            return head != null ? String.join("", head) : "";
        }

        /**
         * Full constructor tolerating a {@code null} body.
         *
         * @param head the head fragments
         * @param body the body fragments
         */
        public SsrResult {
            if (body == null) body = "";
        }
    }
}