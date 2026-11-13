package com.edu.preicfesGratuito.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la API REST.
 *
 * <p>Convierte excepciones tecnicas o de negocio en respuestas JSON
 * consistentes para los clientes.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Procesa errores de validacion de DTOs anotados con Bean Validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                "La solicitud tiene campos inválidos",
                request.getRequestURI(),
                errors
        );
    }

    /**
     * Procesa errores de reglas de negocio asociadas a solicitudes invalidas.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud inválida", exception.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Procesa errores cuando un recurso solicitado no existe.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", exception.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Procesa validaciones de estado de simulacros (ej. ya finalizado, no iniciado)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalStateException(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de estado", exception.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Procesa errores de argumentos como códigos de acceso incorrectos
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Datos incorrectos", exception.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Procesa errores de credenciales invalidas o usuarios inactivos.
     */
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "No autorizado", exception.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Construye la estructura estandar de error.
     */
    private ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
