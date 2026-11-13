package com.edu.preicfesGratuito.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuardarRespuestasRequestDTO {
    
    @NotEmpty(message = "La lista de respuestas no puede estar vacía")
    @Valid
    private List<RespuestaItemDTO> respuestas;
}
