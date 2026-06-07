package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.EstudianteEstadisticasDTO;
import com.edu.preicfesGratuito.dto.EstudianteResumenDTO;
import com.edu.preicfesGratuito.dto.EstudianteSimulacroDTO;

import java.util.List;

/**
 * Contrato de negocio para consultas del modulo de estudiante.
 */
public interface EstudianteService {

    /**
     * Obtiene metricas consolidadas del estudiante.
     *
     * @param idEstudiante identificador del usuario estudiante
     * @return resumen de simulacros realizados, puntaje y tiempo promedio
     */
    EstudianteResumenDTO obtenerResumen(Long idEstudiante);

    /**
     * Consulta los simulacros del periodo activo asignados al estudiante.
     *
     * @param idEstudiante identificador del usuario estudiante
     * @return lista de simulacros realizados y pendientes
     */
    List<EstudianteSimulacroDTO> obtenerSimulacrosPeriodoActivo(Long idEstudiante);

    /**
     * Obtiene datos agregados para tableros y graficas del estudiante.
     *
     * @param idEstudiante identificador del usuario estudiante
     * @return estadisticas globales, por asignatura y por simulacro
     */
    EstudianteEstadisticasDTO obtenerEstadisticas(Long idEstudiante);
}
