package com.edu.preicfesGratuito.repository;

import java.math.BigDecimal;

/**
 * Proyeccion del resumen principal de un estudiante.
 */
public interface EstudianteResumenProjection {
    Long getIdEstudiante();
    String getNombres();
    String getApellidos();
    Long getSimulacrosRealizados();
    BigDecimal getPuntajePromedio();
    BigDecimal getTiempoPromedioSegundos();
}
