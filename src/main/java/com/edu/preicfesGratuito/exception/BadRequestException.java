package com.edu.preicfesGratuito.exception;

/**
 * Excepcion para solicitudes invalidas por reglas de negocio.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje legible para la API.
     *
     * @param message descripcion del error
     */
    public BadRequestException(String message) {
        super(message);
    }
}
