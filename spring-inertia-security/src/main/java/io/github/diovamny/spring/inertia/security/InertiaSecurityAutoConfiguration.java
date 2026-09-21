package io.github.diovamny.spring.inertia.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.inertia.core.spi.FlashStore;
import io.github.diovamny.inertia.core.spi.JsonProvider;
import io.github.diovamny.spring.inertia.version.VersionProvider;

/**
 * Registers the Inertia protocol bridge for Spring Security: helpers only.
 * The application keeps full control of its {@link SecurityFilterChain}
 * (routes, login, authorization); this configuration only exposes the entry
 * point, the denied handler and the optional auth-props contributor as beans
 * to wire into that chain.
 *
 * <p>Only active when Spring Security is present. The core adapter resolves
 * {@code inertia.security.mode=auto} to {@code framework} on this same
 * condition and skips its own CSRF filter.</p>
 */
@AutoConfiguration
@ConditionalOnClass({ HttpSecurity.class, SecurityFilterChain.class })
public class InertiaSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(InertiaProperties.class)
    public InertiaAuthenticationEntryPoint inertiaAuthenticationEntryPoint(
            InertiaProperties properties) {
        return new InertiaAuthenticationEntryPoint(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ InertiaProperties.class, JsonProvider.class, VersionProvider.class,
        FlashStore.class })
    public InertiaAccessDeniedHandler inertiaAccessDeniedHandler(InertiaProperties properties,
            JsonProvider jsonProvider, VersionProvider versionProvider, FlashStore flashStore) {
        return new InertiaAccessDeniedHandler(properties, jsonProvider, versionProvider, flashStore);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "inertia.security", name = "auth-props-enabled", havingValue = "true")
    public InertiaAuthPropsContributor inertiaAuthPropsContributor() {
        return new InertiaAuthPropsContributor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(org.springframework.security.web.csrf.CsrfTokenRepository.class)
    public InertiaCsrfTokenEmitFilter inertiaCsrfTokenEmitFilter(
            org.springframework.security.web.csrf.CsrfTokenRepository repository) {
        return new InertiaCsrfTokenEmitFilter(repository);
    }
}
