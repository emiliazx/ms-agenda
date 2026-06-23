package com.costuras.agenda.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST,   "/agenda/horarios/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/agenda/horarios/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/agenda/horarios/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/agenda/festivos/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/agenda/festivos/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/agenda/festivos/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/agenda/bloqueos/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/agenda/bloqueos/**").hasAuthority("ADMIN")
                .requestMatchers(
                        "/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
