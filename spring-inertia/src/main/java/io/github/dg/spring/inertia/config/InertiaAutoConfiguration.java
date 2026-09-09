package io.github.dg.spring.inertia.config;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import tools.jackson.databind.ObjectMapper;

import io.github.dg.spring.inertia.api.Inertia;
import io.github.dg.spring.inertia.cache.CachedPropStore;
import io.github.dg.spring.inertia.internal.ErrorResponseFactory;
import io.github.dg.spring.inertia.internal.InertiaImpl;
import io.github.dg.spring.inertia.internal.JacksonJsonProvider;
import io.github.dg.spring.inertia.internal.SpringFlashStore;
import io.github.dg.spring.inertia.mvc.ConventionComponentResolver;
import io.github.dg.spring.inertia.mvc.InertiaCsrfFilter;
import io.github.dg.spring.inertia.mvc.InertiaFilter;
import io.github.dg.spring.inertia.mvc.InertiaInterceptor;
import io.github.dg.spring.inertia.mvc.InertiaReturnValueHandler;
import io.github.dg.spring.inertia.nativex.InertiaRuntimeHints;
import io.github.dg.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.dg.spring.inertia.protocol.MergePropProcessor;
import io.github.dg.spring.inertia.protocol.OncePropRegistry;
import io.github.dg.spring.inertia.protocol.PageObjectBuilder;
import io.github.dg.spring.inertia.protocol.PartialReloadProcessor;
import io.github.dg.spring.inertia.protocol.RedirectProcessor;
import io.github.dg.spring.inertia.protocol.ResponseProcessor;
import io.github.dg.spring.inertia.protocol.SharedDataRegistry;
import io.github.dg.spring.inertia.renderer.HtmlRenderer;
import io.github.dg.spring.inertia.renderer.SsrClient;
import io.github.dg.spring.inertia.security.InertiaCsrfService;
import io.github.dg.spring.inertia.spi.ComponentTransformer;
import io.github.dg.spring.inertia.spi.FlashStore;
import io.github.dg.spring.inertia.spi.InertiaSharedDataContributor;
import io.github.dg.spring.inertia.spi.JsonProvider;
import io.github.dg.spring.inertia.spi.UrlResolver;
import io.github.dg.spring.inertia.validation.InertiaValidationHandler;
import io.github.dg.spring.inertia.validation.PrecognitionHandler;
import io.github.dg.spring.inertia.version.ManifestVersionProvider;
import io.github.dg.spring.inertia.version.StaticVersionProvider;
import io.github.dg.spring.inertia.version.VersionProvider;

/**
 * Auto-configuration of the Inertia Spring adapter: registers the JSON
 * provider, the flash store, the version provider, the request-scoped
 * protocol beans, the MVC integration (interceptor, filters, return-value
 * handler) and the validation advice.
 */
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@EnableConfigurationProperties(InertiaProperties.class)
@ImportRuntimeHints(InertiaRuntimeHints.class)
public class InertiaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CachedPropStore cachedPropStore() {
        return new CachedPropStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonProvider inertiaJsonProvider(ObjectMapper objectMapper) {
        return new JacksonJsonProvider(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlashStore inertiaFlashStore() {
        return new SpringFlashStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public VersionProvider inertiaVersionProvider(InertiaProperties properties) {
        if ("vite-manifest".equalsIgnoreCase(properties.getVersionStrategy())) {
            return new ManifestVersionProvider();
        }
        return new StaticVersionProvider(properties);
    }

    @Bean
    public InertiaConfigValidator inertiaConfigValidator(InertiaProperties properties) {
        return new InertiaConfigValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public InertiaHeaderExtractor inertiaHeaderExtractor() {
        return new InertiaHeaderExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public PartialReloadProcessor partialReloadProcessor() {
        return new PartialReloadProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MergePropProcessor mergePropProcessor() {
        return new MergePropProcessor();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SharedDataRegistry sharedDataRegistry() {
        return new SharedDataRegistry();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public OncePropRegistry oncePropRegistry() {
        return new OncePropRegistry();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RedirectProcessor redirectProcessor(FlashStore flashStore) {
        return new RedirectProcessor(flashStore);
    }

    @Bean
    @ConditionalOnMissingBean(ComponentTransformer.class)
    @ConditionalOnProperty(prefix = "inertia", name = "convention-routing-enabled", havingValue = "true")
    public ComponentTransformer conventionComponentResolver(InertiaProperties properties) {
        return new ConventionComponentResolver(properties);
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public PageObjectBuilder pageObjectBuilder(InertiaProperties properties,
            SharedDataRegistry sharedDataRegistry,
            OncePropRegistry oncePropRegistry,
            PartialReloadProcessor partialReloadProcessor,
            MergePropProcessor mergePropProcessor,
            FlashStore flashStore,
            VersionProvider versionProvider,
            ObjectProvider<ComponentTransformer> componentTransformer,
            ObjectProvider<UrlResolver> urlResolver) {
        return new PageObjectBuilder(properties, sharedDataRegistry, oncePropRegistry,
            partialReloadProcessor, mergePropProcessor, flashStore, versionProvider,
            componentTransformer.getIfAvailable(), urlResolver.getIfAvailable());
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ResponseProcessor responseProcessor(PageObjectBuilder pageObjectBuilder,
            JsonProvider jsonProvider, HtmlRenderer htmlRenderer, InertiaProperties properties) {
        return new ResponseProcessor(pageObjectBuilder, jsonProvider, htmlRenderer, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SsrClient ssrClient(InertiaProperties properties, JsonProvider jsonProvider) {
        return new SsrClient(properties, jsonProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public HtmlRenderer htmlRenderer(InertiaProperties properties, JsonProvider jsonProvider,
            SsrClient ssrClient) {
        return new HtmlRenderer(properties, jsonProvider, ssrClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorResponseFactory errorResponseFactory(JsonProvider jsonProvider,
            InertiaProperties properties) {
        return new ErrorResponseFactory(jsonProvider, properties);
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Inertia inertia(InertiaProperties properties, SharedDataRegistry sharedDataRegistry,
            OncePropRegistry oncePropRegistry, PartialReloadProcessor partialReloadProcessor,
            RedirectProcessor redirectProcessor, ResponseProcessor responseProcessor,
            FlashStore flashStore, CachedPropStore cachedPropStore, VersionProvider versionProvider) {
        return new InertiaImpl(properties, sharedDataRegistry, oncePropRegistry,
            partialReloadProcessor, redirectProcessor, responseProcessor, flashStore,
            cachedPropStore, versionProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public InertiaCsrfService inertiaCsrfService() {
        return new InertiaCsrfService();
    }

    @Bean
    @ConditionalOnMissingBean
    public InertiaCsrfFilter inertiaCsrfFilter(InertiaProperties properties,
            InertiaCsrfService csrfService) {
        return new InertiaCsrfFilter(properties, csrfService);
    }

    @Bean
    @ConditionalOnMissingBean
    public InertiaFilter inertiaFilter(InertiaProperties properties) {
        return new InertiaFilter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public InertiaInterceptor inertiaInterceptor(
            InertiaHeaderExtractor headerExtractor,
            ObjectProvider<List<InertiaSharedDataContributor>> contributorsProvider,
            ObjectProvider<SharedDataRegistry> sharedDataRegistryProvider) {
        return new InertiaInterceptor(headerExtractor, contributorsProvider, sharedDataRegistryProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public InertiaReturnValueHandler inertiaReturnValueHandler(ResponseProcessor responseProcessor) {
        return new InertiaReturnValueHandler(responseProcessor);
    }

    @Bean
    public InertiaWebMvcConfigurer inertiaWebMvcConfigurer(InertiaInterceptor interceptor,
            InertiaReturnValueHandler returnValueHandler) {
        return new InertiaWebMvcConfigurer(interceptor, returnValueHandler);
    }

    @Bean
    public PrecognitionHandler precognitionHandler(JsonProvider jsonProvider, FlashStore flashStore) {
        return new PrecognitionHandler(jsonProvider, flashStore);
    }

    @Bean
    public InertiaValidationHandler inertiaValidationHandler(ErrorResponseFactory errorResponseFactory) {
        return new InertiaValidationHandler(errorResponseFactory);
    }
}
