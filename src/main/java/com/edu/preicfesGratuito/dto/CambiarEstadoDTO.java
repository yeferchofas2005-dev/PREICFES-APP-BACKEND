package com.edu.preicfesGratuito.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Solicitud para activar o inactivar una entidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoDTO {

    @NotNull(message = "El estado activo es requerido")
    private Boolean activo;
}
