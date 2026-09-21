package com.example.kitchensink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.AuthService;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.security.InertiaAccessDeniedHandler;
import io.github.diovamny.spring.inertia.security.InertiaAuthenticationEntryPoint;
import io.github.diovamny.spring.inertia.security.InertiaCsrfConfigurer;

/**
 * Application security: public pages and health stay open, everything else
 * requires authentication. CSRF, challenges and denials are owned by Spring
 * Security through the Inertia bridge (mode {@code framework}).
 */
@Configuration
@EnableWebSecurity
public class DemoSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return AuthService.hash(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return AuthService.matches(rawPassword.toString(), encodedPassword);
            }
        };
    }

    @Bean
    UserDetailsService users(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
            .map(user -> User.withUsername(user.email)
                .password(user.password)
                .authorities("ROLE_USER")
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CookieCsrfTokenRepository xsrfTokenRepository(InertiaProperties properties) {
        return InertiaCsrfConfigurer.xsrfTokenRepository(properties.getSecurity().getCookiePath());
    }

    @Bean
    SecurityFilterChain demoChain(HttpSecurity http,
            CookieCsrfTokenRepository xsrfTokenRepository,
            InertiaAuthenticationEntryPoint entryPoint,
            InertiaAccessDeniedHandler deniedHandler) throws Exception {
        http
            .csrf(csrf -> InertiaCsrfConfigurer.configure(csrf, xsrfTokenRepository))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/assets/**", "/favicon.svg", "/actuator/health").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
