package com.edu.preicfesGratuito.enums;

/**
 * Roles funcionales reconocidos por la aplicacion.
 *
 * <p>Cada valor corresponde al nombre almacenado en la tabla {@code rol}
 * y se usa como prefijo {@code ROLE_*} en Spring Security.</p>
 */
public enum RolNombre {

    /** Administrador con acceso total al sistema. */
    ADMIN,

    /** Directivo institucional con acceso a gestion y reportes. */
    DIRECTIVO,

    /** Profesor que dirige grupos y asigna simulacros. */
    PROFESOR,

    /** Estudiante que resuelve simulacros y consulta estadisticas. */
    ESTUDIANTE
}
