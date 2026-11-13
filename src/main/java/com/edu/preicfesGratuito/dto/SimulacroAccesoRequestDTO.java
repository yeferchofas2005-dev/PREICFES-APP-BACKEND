package com.edu.preicfesGratuito.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulacroAccesoRequestDTO {
    @NotBlank(message = "El código de acceso es obligatorio")
    @Size(min = 5, max = 5, message = "El código de acceso debe tener 5 caracteres")
    private String codigoAcceso;
}
