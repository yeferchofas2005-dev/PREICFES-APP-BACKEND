package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.IntentoSimulacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de intentos de simulacro.
 *
 * <p>Incluye consultas nativas agregadas para el dashboard del estudiante. Se
 * usan proyecciones para traer solo las columnas necesarias y reducir memoria.</p>
 */
@Repository
public interface IntentoSimulacroRepository extends JpaRepository<IntentoSimulacro, Long> {

    /**
     * Calcula el resumen de intentos finalizados de un estudiante.
     *
     * @param idEstudiante identificador del estudiante
     * @return proyeccion con cantidad de simulacros, puntaje promedio y tiempo promedio
     */
    @Query(value = """
            SELECT
                u.id_usuario AS idEstudiante,
                u.nombres AS nombres,
                u.apellidos AS apellidos,
                COUNT(i.id_intento) AS simulacrosRealizados,
                COALESCE(ROUND(AVG(i.puntaje_total), 2), 0) AS puntajePromedio,
                COALESCE(ROUND(AVG(i.tiempo_total_segundos), 2), 0) AS tiempoPromedioSegundos
            FROM usuario u
            LEFT JOIN intento_simulacro i
                ON i.id_estudiante = u.id_usuario
                AND i.fecha_fin IS NOT NULL
            WHERE u.id_usuario = :idEstudiante
            GROUP BY u.id_usuario, u.nombres, u.apellidos
            """, nativeQuery = true)
    EstudianteResumenProjection findResumenByEstudianteId(@Param("idEstudiante") Long idEstudiante);

    /**
     * Obtiene las metricas globales del estudiante y el promedio nacional.
     *
     * @param idEstudiante identificador del estudiante
     * @return proyeccion con puntaje global, porcentaje nacional y tiempo promedio
     */
    @Query(value = """
            SELECT
                COALESCE(ROUND(AVG(i.puntaje_total), 2), 0) AS puntajeGlobal,
                (
                    SELECT COALESCE(ROUND(AVG(inacional.porcentaje_aciertos), 2), 0)
                    FROM intento_simulacro inacional
                    WHERE inacional.fecha_fin IS NOT NULL
                ) AS porcentajeNacional,
                COALESCE(ROUND(AVG(i.tiempo_total_segundos), 2), 0) AS tiempoPromedioSegundos
            FROM intento_simulacro i
            WHERE i.id_estudiante = :idEstudiante
                AND i.fecha_fin IS NOT NULL
            """, nativeQuery = true)
    EstudianteEstadisticasGeneralesProjection findEstadisticasGeneralesByEstudianteId(
            @Param("idEstudiante") Long idEstudiante
    );

    /**
     * Calcula el desempeno promedio por area para graficas tipo radar/pentagono.
     *
     * @param idEstudiante identificador del estudiante
     * @return lista de areas con promedio de acierto en porcentaje
     */
    @Query(value = """
            SELECT
                a.id_area AS idArea,
                a.nombre AS asignatura,
                COALESCE(ROUND(AVG(CASE WHEN r.es_correcta = TRUE THEN 100 ELSE 0 END), 2), 0) AS puntajePromedio
            FROM respuesta_estudiante r
            INNER JOIN intento_simulacro i ON i.id_intento = r.id_intento
            INNER JOIN pregunta p ON p.id_pregunta = r.id_pregunta
            INNER JOIN banco_preguntas b ON b.id_banco = p.id_banco
            INNER JOIN area a ON a.id_area = b.id_area
            WHERE i.id_estudiante = :idEstudiante
                AND i.fecha_fin IS NOT NULL
            GROUP BY a.id_area, a.nombre
            ORDER BY a.id_area
            """, nativeQuery = true)
    List<AsignaturaDesempenoProjection> findDesempenoPorAsignaturaByEstudianteId(
            @Param("idEstudiante") Long idEstudiante
    );

    /**
     * Calcula el tiempo promedio de respuesta por area.
     *
     * @param idEstudiante identificador del estudiante
     * @return lista de areas con tiempo promedio en segundos
     */
    @Query(value = """
            SELECT
                a.id_area AS idArea,
                a.nombre AS asignatura,
                COALESCE(ROUND(AVG(r.tiempo_respuesta_segundos), 2), 0) AS tiempoPromedioSegundos
            FROM respuesta_estudiante r
            INNER JOIN intento_simulacro i ON i.id_intento = r.id_intento
            INNER JOIN pregunta p ON p.id_pregunta = r.id_pregunta
            INNER JOIN banco_preguntas b ON b.id_banco = p.id_banco
            INNER JOIN area a ON a.id_area = b.id_area
            WHERE i.id_estudiante = :idEstudiante
                AND i.fecha_fin IS NOT NULL
                AND r.tiempo_respuesta_segundos IS NOT NULL
            GROUP BY a.id_area, a.nombre
            ORDER BY a.id_area
            """, nativeQuery = true)
    List<AsignaturaTiempoProjection> findTiempoPorAsignaturaByEstudianteId(
            @Param("idEstudiante") Long idEstudiante
    );

    /**
     * Calcula el resultado del estudiante por area dentro de cada simulacro.
     *
     * @param idEstudiante identificador del estudiante
     * @return serie de resultados agrupada por simulacro y asignatura
     */
    @Query(value = """
            SELECT
                s.id_simulacro AS idSimulacro,
                s.nombre AS simulacro,
                a.id_area AS idArea,
                a.nombre AS asignatura,
                COALESCE(ROUND(AVG(CASE WHEN r.es_correcta = TRUE THEN 100 ELSE 0 END), 2), 0) AS puntaje
            FROM respuesta_estudiante r
            INNER JOIN intento_simulacro i ON i.id_intento = r.id_intento
            INNER JOIN simulacro s ON s.id_simulacro = i.id_simulacro
            INNER JOIN pregunta p ON p.id_pregunta = r.id_pregunta
            INNER JOIN banco_preguntas b ON b.id_banco = p.id_banco
            INNER JOIN area a ON a.id_area = b.id_area
            WHERE i.id_estudiante = :idEstudiante
                AND i.fecha_fin IS NOT NULL
            GROUP BY s.id_simulacro, s.nombre, a.id_area, a.nombre
            ORDER BY s.id_simulacro, a.id_area
            """, nativeQuery = true)
    List<ResultadoSimulacroAsignaturaProjection> findResultadosPorSimulacroYAsignaturaByEstudianteId(
            @Param("idEstudiante") Long idEstudiante
    );
}
