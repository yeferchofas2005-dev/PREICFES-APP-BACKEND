package com.edu.preicfesGratuito.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Punto de entrada para errores de autenticacion.
 *
 * <p>Devuelve una respuesta JSON 401 cuando un cliente intenta acceder a un
 * recurso protegido sin token valido.</p>
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Construye la respuesta de error para accesos no autenticados.
     *
     * @param request peticion que fallo
     * @param response respuesta HTTP a completar
     * @param authException excepcion de autenticacion generada por Spring Security
     * @throws IOException si no se puede escribir la respuesta
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String body = """
                {"timestamp":"%s","status":401,"error":"No autorizado","message":"Token ausente, inválido o expirado","path":"%s"}
                """.formatted(LocalDateTime.now(), request.getRequestURI());

        response.getWriter().write(body);
    }
}
