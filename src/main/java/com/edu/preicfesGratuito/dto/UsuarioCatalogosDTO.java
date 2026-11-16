package com.edu.preicfesGratuito.dto;

import lombok.*;
import java.util.List;

/**
 * Catalogos permitidos para los formularios de gestion de usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCatalogosDTO {
    private List<RolDTO> roles;
    private List<NodoSimpleDTO> nodos;
}
