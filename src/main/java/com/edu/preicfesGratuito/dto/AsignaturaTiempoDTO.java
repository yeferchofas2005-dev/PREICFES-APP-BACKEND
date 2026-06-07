package com.edu.preicfesGratuito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Punto de datos para graficar tiempo promedio por asignatura.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaTiempoDTO {
    private Long idArea;
    private String asignatura;
    private BigDecimal tiempoPromedioSegundos;
}
