package com.microservicios.notificaciones_service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter
    
    WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers('/swagger-ui/**', '/swagger-ui.html', '/v3/api-docs/**').permitAll()
                    .requestMatchers('/actuator/**').permitAll()
                    // Solo ADMIN puede crear notificaciones (POST)
                    .requestMatchers(HttpMethod.POST, '/notificaciones/**').hasAuthority('ADMIN')
                    // USER y ADMIN pueden consultar (GET)
                    .requestMatchers(HttpMethod.GET, '/notificaciones/**').authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        
        return http.build()
    }
}
