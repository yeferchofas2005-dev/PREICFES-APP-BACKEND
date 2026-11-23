package com.edu.preicfesGratuito.util;

import java.util.regex.Pattern;

/**
 * Expresiones regulares compartidas para validar datos de usuarios en DTOs y carga masiva.
 */
public final class UsuarioValidationPatterns {

    public static final String NOMBRE_PERSONA_REGEX = "^\\s*[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+(?:[\\s'\\-][a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+)*\\s*$";
    public static final String DOCUMENTO_REGEX = "^\\s*[0-9]{5,20}\\s*$";
    public static final String CORREO_REGEX = "^\\s*[A-Za-zñÑ0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\s*$";
    public static final String TELEFONO_OPCIONAL_REGEX = "^(\\s*|\\s*\\+?[0-9]{7,15}\\s*)$";

    public static final Pattern NOMBRE_PERSONA_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+(?:[\\s'\\-][a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+)*$");
    public static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^[0-9]{5,20}$");
    public static final Pattern CORREO_PATTERN = Pattern.compile("^[A-Za-zñÑ0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private UsuarioValidationPatterns() {
    }
}
