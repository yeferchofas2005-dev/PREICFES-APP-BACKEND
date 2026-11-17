package com.edu.preicfesGratuito.dto;

import lombok.*;

/**
 * Detalle de error de una fila durante la carga masiva.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargaMasivaErrorDTO {
    private int fila;
    private String documento;
    private String correo;
    private String motivo;
}
