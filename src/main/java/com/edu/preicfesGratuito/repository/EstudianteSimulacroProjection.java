package com.edu.preicfesGratuito.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyeccion ligera de simulacros visibles para un estudiante.
 */
public interface EstudianteSimulacroProjection {
    Long getIdSimulacro();
    String getNombre();
    LocalDateTime getFecha();
    String getEstado();
    BigDecimal getPuntaje();
    Integer getTiempoSegundos();
    BigDecimal getPorcentajeAprobacion();
}
