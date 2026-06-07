package com.edu.preicfesGratuito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Resumen de rendimiento del estudiante autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResumenDTO {
    private Long idEstudiante;
    private String nombres;
    private String apellidos;
    private Long simulacrosRealizados;
    private BigDecimal puntajePromedio;
    private BigDecimal tiempoPromedioSegundos;
}
