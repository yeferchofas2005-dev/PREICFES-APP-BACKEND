package com.edu.preicfesGratuito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta comun para login y registro.
 *
 * <p>Incluye el token JWT emitido y una representacion segura del usuario.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private UsuarioResponseDTO usuario;
}
