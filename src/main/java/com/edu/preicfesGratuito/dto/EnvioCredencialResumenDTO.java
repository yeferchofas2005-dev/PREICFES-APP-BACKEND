package com.edu.preicfesGratuito.dto;

import lombok.*;

/**
 * Resumen de avance de la cola de envio de credenciales y cuota diaria global.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvioCredencialResumenDTO {
    private long totalRegistros;
    private long enviados;
    private long pendientes;
    private long procesando;
    private long fallidos;
    private double porcentajeAvance;
    private long enviadosHoyGlobal;
    private int limiteDiario;
    private long cupoRestanteHoy;
    private boolean loteEnProceso;
    private int diasEstimadosRestantes;
}
