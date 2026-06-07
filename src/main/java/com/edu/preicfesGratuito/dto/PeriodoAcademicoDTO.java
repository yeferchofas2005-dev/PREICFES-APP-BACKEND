package com.edu.preicfesGratuito.dto;

import lombok.*;
import java.time.LocalDate;

/**
 * Representacion de un periodo academico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademicoDTO {
    private Long idPeriodo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
