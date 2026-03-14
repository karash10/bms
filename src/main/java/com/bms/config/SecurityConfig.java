package com.bms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.bms.security.JwtAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .headers(headers ->
                headers.frameOptions(frame -> frame.disable())
            )

            .authorizeHttpRequests(auth -> auth
                // H2 console is served by a servlet; use servlet PathPattern matcher.
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/h2-console")).permitAll()
                .requestMatchers(PathPatternRequestMatcher.pathPattern("/h2-console/**")).permitAll()

                // Static resources & pages
                .requestMatchers("/", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login", "/register", "/logout").permitAll()

                // Public browsing pages
                .requestMatchers("/events", "/events/**").permitAll()
                .requestMatchers("/shows", "/shows/**").permitAll()

                // Authenticated user pages
                .requestMatchers("/my-bookings").authenticated()
                .requestMatchers("/bookings/**").authenticated()

                // Admin pages
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")

                // REST API
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/events/**").permitAll()
                .requestMatchers("/api/shows/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/bookings/**").hasRole("USER")
                .requestMatchers("/api/payments/**").hasRole("USER")

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
