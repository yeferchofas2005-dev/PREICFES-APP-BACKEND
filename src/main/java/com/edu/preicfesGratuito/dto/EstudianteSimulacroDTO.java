package com.edu.preicfesGratuito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simulacro visible para el estudiante en el periodo academico activo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteSimulacroDTO {
    private Long idSimulacro;
    private String nombre;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal puntaje;
    private Integer tiempoSegundos;
    private BigDecimal porcentajeAprobacion;
}
