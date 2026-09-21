package io.github.diovamny.spring.inertia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import io.github.diovamny.spring.inertia.config.InertiaProperties;

/**
 * Reference application chain for the security integration tests: public
 * pages, an ADMIN-only area, session fixation on login, and framework-owned
 * CSRF with Inertia cookie/header names.
 */
@Configuration
@EnableWebSecurity
class TestWebSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("user@example.com").password(encoder.encode("password"))
                .authorities("ROLE_USER").build(),
            User.withUsername("admin@example.com").password(encoder.encode("adminpass"))
                .authorities("ROLE_USER", "ROLE_ADMIN").build());
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    org.springframework.security.web.csrf.CookieCsrfTokenRepository xsrfTokenRepository(
            InertiaProperties properties) {
        return InertiaCsrfConfigurer.xsrfTokenRepository(properties.getSecurity().getCookiePath());
    }

    @Bean
    SecurityFilterChain testChain(HttpSecurity http,
            org.springframework.security.web.csrf.CookieCsrfTokenRepository xsrfTokenRepository,
            InertiaAuthenticationEntryPoint entryPoint,
            InertiaAccessDeniedHandler deniedHandler) throws Exception {
        http
            .csrf(csrf -> InertiaCsrfConfigurer.configure(csrf, xsrfTokenRepository))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public", "/login-json", "/error").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated())
            .sessionManagement(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
