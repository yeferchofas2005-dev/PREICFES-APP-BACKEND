package com.edu.preicfesGratuito.dto;

import lombok.*;

/**
 * Representacion resumida de un nodo operativo con su localidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodoSimpleDTO {
    private Long idNodo;
    private String nombre;
    private String localidad;
}
