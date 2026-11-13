package com.edu.preicfesGratuito.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaItemDTO {
    @NotNull(message = "El id de la pregunta es requerido")
    private Long idPregunta;
    
    @NotNull(message = "El id de la opción es requerido")
    private Long idOpcion;
    
    private Integer tiempoRespuestaSegundos;
}
