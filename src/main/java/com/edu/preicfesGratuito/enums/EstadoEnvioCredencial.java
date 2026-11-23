package com.edu.preicfesGratuito.enums;

/**
 * Estados del ciclo de vida de un envio de credenciales en cola.
 */
public enum EstadoEnvioCredencial {
    PENDIENTE,
    PROCESANDO,
    ENVIADO,
    FALLIDO
}
