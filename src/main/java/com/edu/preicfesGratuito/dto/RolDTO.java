package com.edu.preicfesGratuito.dto;

import lombok.*;

/**
 * Representacion simple de un rol del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolDTO {
    private Long idRol;
    private String nombre;
}
