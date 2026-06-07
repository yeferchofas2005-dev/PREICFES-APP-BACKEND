package com.edu.preicfesGratuito.config;

import com.edu.preicfesGratuito.security.JwtAuthenticationEntryPoint;
import com.edu.preicfesGratuito.security.JwtAuthenticationFilter;
import com.edu.preicfesGratuito.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion central de Spring Security.
 *
 * <p>Define una API sin estado basada en JWT, expone los endpoints publicos
 * de autenticacion y restringe los prefijos funcionales segun el rol del
 * usuario autenticado.</p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Construye la cadena de filtros HTTP de seguridad.
     *
     * <p>Desactiva CSRF porque la API no usa sesiones de navegador, configura
     * sesiones stateless, registra el manejador de errores 401 y ubica el filtro
     * JWT antes del filtro de autenticacion por usuario y contrasena.</p>
     *
     * @param http builder de seguridad HTTP proporcionado por Spring
     * @return cadena de filtros lista para proteger la API
     * @throws Exception si Spring Security no puede construir la configuracion
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/directivo/**").hasAnyRole("ADMIN", "DIRECTIVO")
                        .requestMatchers("/api/profesor/**").hasAnyRole("ADMIN", "DIRECTIVO", "PROFESOR")
                        .requestMatchers("/api/estudiante/**").hasAnyRole("ADMIN", "DIRECTIVO", "PROFESOR", "ESTUDIANTE")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Proveedor de autenticacion que valida credenciales contra la base de datos.
     *
     * @return proveedor DAO con {@link PasswordEncoder} BCrypt
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el {@link AuthenticationManager} usado por el servicio de login.
     *
     * @param configuration configuracion de autenticacion de Spring
     * @return administrador de autenticacion de la aplicacion
     * @throws Exception si no se puede resolver el manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Codificador de contrasenas para persistir y verificar hashes seguros.
     *
     * @return instancia BCrypt usada en registro y login
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
