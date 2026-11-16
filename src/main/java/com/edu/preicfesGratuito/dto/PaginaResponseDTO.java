package com.edu.preicfesGratuito.dto;

import lombok.*;
import java.util.List;

/**
 * Respuesta paginada estandar para listados de consulta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginaResponseDTO<T> {
    private List<T> contenido;
    private int paginaActual;
    private int tamanoPagina;
    private long totalElementos;
    private int totalPaginas;
    private boolean ultima;
}
