package com.edu.preicfesGratuito.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaExamenDTO {
    private Long idArea;
    private String nombre;
    private Integer totalPreguntas;
}
