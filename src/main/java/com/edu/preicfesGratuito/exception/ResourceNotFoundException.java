package com.edu.preicfesGratuito.exception;

/**
 * Excepcion para recursos que no existen en la base de datos.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje legible para la API.
     *
     * @param message descripcion del recurso no encontrado
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
