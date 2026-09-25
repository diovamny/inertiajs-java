package com.example.pingcrm;

import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.pingcrm.config.AuthInterceptor;
import com.example.pingcrm.service.AuthService;
import io.github.diovamny.spring.inertia.config.InertiaProperties;
import io.github.diovamny.spring.inertia.security.InertiaAccessDeniedHandler;
import io.github.diovamny.spring.inertia.security.InertiaAuthenticationEntryPoint;
import io.github.diovamny.spring.inertia.security.InertiaCsrfConfigurer;

@SpringBootApplication
@EnableWebSecurity
@EnableMethodSecurity
public class PingCrmSvelteApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(PingCrmSvelteApplication.class, args);
    }

    private final AuthInterceptor authInterceptor;

    public PingCrmSvelteApplication(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

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
    UserDetailsService userDetailsService(com.example.pingcrm.repository.UserRepository userRepository) {
        return username -> {
            var user = userRepository.findByEmail(username).orElse(null);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            var authorities = new ArrayList<SimpleGrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            if (user.owner) {
                authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
            }
            return User.withUsername(user.email)
                .password(user.password)
                .authorities(authorities)
                .build();
        };
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
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            CookieCsrfTokenRepository xsrfTokenRepository,
            InertiaAuthenticationEntryPoint entryPoint,
            InertiaAccessDeniedHandler deniedHandler) throws Exception {
        http
            .csrf(csrf -> InertiaCsrfConfigurer.configure(csrf, xsrfTokenRepository))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/e2e-probe/**", "/assets/**", "/favicon.svg").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/favicon.svg")
            .addResourceLocations("classpath:/static/favicon.svg");
    }
}
