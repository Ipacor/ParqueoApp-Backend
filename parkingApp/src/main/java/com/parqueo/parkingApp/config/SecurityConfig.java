package com.parqueo.parkingApp.config;

import com.parqueo.parkingApp.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                // Permitir acceso público al login
                .requestMatchers("/api/usuarios/login").permitAll()
                // Vehículos
                .requestMatchers("/api/vehiculos/**").hasAnyAuthority("VEHICULO_LEER", "VEHICULO_CREAR", "VEHICULO_EDITAR", "VEHICULO_ELIMINAR")
                // Reglas de Estacionamiento
                .requestMatchers("/api/reglas/**").hasAnyAuthority("REGLA_LEER", "REGLA_CREAR", "REGLA_EDITAR", "REGLA_ELIMINAR")
                // Sanciones
                .requestMatchers("/api/sanciones/usuario/**").hasAnyAuthority("SANCION_LEER", "SANCION_CREAR", "SANCION_EDITAR", "SANCION_ELIMINAR")
                .requestMatchers("/api/sanciones/**").hasAnyAuthority("SANCION_LEER", "SANCION_CREAR", "SANCION_EDITAR", "SANCION_ELIMINAR")
                // Usuarios (excepto login)
                .requestMatchers("/api/usuarios/**").hasAnyAuthority("USUARIO_LEER", "USUARIO_CREAR", "USUARIO_EDITAR", "USUARIO_ELIMINAR")
                // Otros endpoints protegidos
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .successHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                })
                .failureHandler((request, response, exception) -> {
                    Throwable realException = exception;
                    if (exception.getCause() != null) {
                        realException = exception.getCause();
                    }
                    if (realException instanceof LockedException) {
                        response.setStatus(423); // 423 Locked
                        response.setContentType("text/plain;charset=UTF-8");
                        response.getWriter().write(realException.getMessage());
                    } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("text/plain;charset=UTF-8");
                        response.getWriter().write(exception.getMessage());
                    }
                })
            .and()
            .httpBasic();
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:8081", "http://10.0.2.2:8081", "http://localhost:3000", "http://127.0.0.1:8081")
                    .allowCredentials(true)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
} 