package com.edu.preicfesGratuito.repository;

/**
 * Proyeccion agregada con los conteos de estado de la cola de envios.
 */
public interface EnvioCredencialResumenProjection {
    long getTotalRegistros();
    long getEnviados();
    long getPendientes();
    long getProcesando();
    long getFallidos();
}
