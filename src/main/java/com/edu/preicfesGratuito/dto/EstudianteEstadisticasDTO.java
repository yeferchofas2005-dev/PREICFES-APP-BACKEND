package com.edu.preicfesGratuito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos agregados para construir el dashboard de estadisticas del estudiante.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteEstadisticasDTO {

    /** Promedio de puntaje total en intentos finalizados del estudiante. */
    private BigDecimal puntajeGlobal;

    /** Promedio nacional de porcentaje de aciertos en todos los intentos finalizados. */
    private BigDecimal porcentajeNacional;

    /** Tiempo promedio en segundos por intento finalizado. */
    private BigDecimal tiempoPromedioSegundos;

    /** Promedio de acierto por asignatura; alimenta graficas tipo radar. */
    private List<AsignaturaDesempenoDTO> desempenoPorAsignatura;

    /** Tiempo promedio de respuesta por asignatura. */
    private List<AsignaturaTiempoDTO> tiempoPorAsignatura;

    /** Puntaje del estudiante desglosado por simulacro y asignatura. */
    private List<ResultadoSimulacroAsignaturaDTO> resultadoSimulacro;
}
