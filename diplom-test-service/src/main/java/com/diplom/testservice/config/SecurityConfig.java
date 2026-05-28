package com.diplom.testservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/actuator/**")
                .requestMatchers("/health/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // API endpoints - публичные GET, админские POST/PUT/DELETE
                        .requestMatchers(HttpMethod.GET, "/api/tests/**", "/api/ab/**").permitAll()
                        .requestMatchers("/api/proxy/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tests/**", "/api/ab/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tests/**", "/api/ab/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/tests/**", "/api/ab/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tests/**", "/api/ab/**").hasRole("ADMIN")
                        // Все остальное требует аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.admin.username:admin}") String adminUsername,
            @Value("${app.security.admin.password:Admin1234!}") String adminPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(passwordEncoder().encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
