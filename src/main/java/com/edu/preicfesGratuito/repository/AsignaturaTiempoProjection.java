package com.edu.preicfesGratuito.repository;

import java.math.BigDecimal;

/**
 * Proyeccion del tiempo promedio de respuesta por asignatura.
 */
public interface AsignaturaTiempoProjection {
    Long getIdArea();
    String getAsignatura();
    BigDecimal getTiempoPromedioSegundos();
}
