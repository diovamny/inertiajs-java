package com.example.pingcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableWebSecurity
public class PingCrmSvelteApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(PingCrmSvelteApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsManager userDetailsManager(com.example.pingcrm.repository.UserRepository userRepository, PasswordEncoder encoder) {
        return new UserDetailsManager() {
            @Override
            public void createUser(UserDetails user) {
                throw new UnsupportedOperationException("Use repository directly");
            }

            @Override
            public void updateUser(UserDetails user) {
                throw new UnsupportedOperationException("Use repository directly");
            }

            @Override
            public void deleteUser(String username) {
                throw new UnsupportedOperationException("Use repository directly");
            }

            @Override
            public void changePassword(String oldPassword, String newPassword) {
                throw new UnsupportedOperationException("Use repository directly");
            }

            @Override
            public boolean userExists(String username) {
                return userRepository.findByEmail(username).isPresent();
            }

            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username)
                    .map(u -> User.withUsername(u.email)
                        .password(u.password)
                        .authorities("ROLE_USER")
                        .build())
                    .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
            }
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        return http.build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/favicon.svg")
            .addResourceLocations("classpath:/static/favicon.svg");
    }
}
