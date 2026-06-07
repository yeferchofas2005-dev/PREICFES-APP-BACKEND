package com.edu.preicfesGratuito.repository;

import java.math.BigDecimal;

/**
 * Proyeccion del puntaje obtenido por asignatura en cada simulacro.
 */
public interface ResultadoSimulacroAsignaturaProjection {
    Long getIdSimulacro();
    String getSimulacro();
    Long getIdArea();
    String getAsignatura();
    BigDecimal getPuntaje();
}
