package com.edu.preicfesGratuito.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaExamenDTO {
    private Long idPregunta;
    private String enunciado;
    private String rutaImagen;
    private Integer nivelDificultad;
    
    private CasoExamenDTO caso;
    private List<OpcionExamenDTO> opciones;
}
