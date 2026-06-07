package com.edu.preicfesGratuito.repository;

import java.math.BigDecimal;

/**
 * Proyeccion del desempeno promedio de un estudiante por asignatura.
 */
public interface AsignaturaDesempenoProjection {
    Long getIdArea();
    String getAsignatura();
    BigDecimal getPuntajePromedio();
}
