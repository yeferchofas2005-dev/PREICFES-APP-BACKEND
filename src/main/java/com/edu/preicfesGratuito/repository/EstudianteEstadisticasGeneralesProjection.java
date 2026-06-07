package com.edu.preicfesGratuito.repository;

import java.math.BigDecimal;

/**
 * Proyeccion de metricas globales para el dashboard del estudiante.
 */
public interface EstudianteEstadisticasGeneralesProjection {
    BigDecimal getPuntajeGlobal();
    BigDecimal getPorcentajeNacional();
    BigDecimal getTiempoPromedioSegundos();
}
