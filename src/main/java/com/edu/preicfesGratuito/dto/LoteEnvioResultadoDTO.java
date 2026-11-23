package com.edu.preicfesGratuito.dto;

import lombok.*;

/**
 * Respuesta inmediata al solicitar el procesamiento del lote diario o reintento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteEnvioResultadoDTO {
    private boolean iniciado;
    private String mensaje;
    private long cupoRestanteHoy;
    private long pendientesEnCola;
    private int registrosAfectados;
}
