package com.edu.preicfesGratuito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Resultado de una asignatura dentro de un simulacro realizado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoSimulacroAsignaturaDTO {
    private Long idSimulacro;
    private String simulacro;
    private Long idArea;
    private String asignatura;
    private BigDecimal puntaje;
}
