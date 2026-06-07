package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Simulacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de simulacros.
 *
 * <p>Ademas de operaciones CRUD, incluye consultas nativas para listar
 * simulacros del periodo activo visibles para un estudiante segun su grupo.</p>
 */
@Repository
public interface SimulacroRepository extends JpaRepository<Simulacro, Long> {

    /**
     * Lista los simulacros activos del periodo vigente asignados al grupo del estudiante.
     *
     * <p>La consulta marca cada simulacro como realizado cuando existe un intento
     * finalizado del estudiante. Si hay varios intentos, toma el mas reciente.</p>
     *
     * @param idEstudiante identificador del estudiante
     * @return simulacros visibles con estado y datos del ultimo intento finalizado
     */
    @Query(value = """
            SELECT
                s.id_simulacro AS idSimulacro,
                s.nombre AS nombre,
                s.fecha_creacion AS fecha,
                CASE WHEN i.id_intento IS NULL THEN 'NO_REALIZADO' ELSE 'REALIZADO' END AS estado,
                i.puntaje_total AS puntaje,
                i.tiempo_total_segundos AS tiempoSegundos,
                i.porcentaje_aciertos AS porcentajeAprobacion
            FROM grupo_estudiante ge
            INNER JOIN simulacro_grupo sg ON sg.id_grupo = ge.id_grupo
            INNER JOIN simulacro s ON s.id_simulacro = sg.id_simulacro
            INNER JOIN periodo_academico p ON p.id_periodo = s.id_periodo
            LEFT JOIN (
                SELECT i1.*
                FROM intento_simulacro i1
                INNER JOIN (
                    SELECT id_simulacro, MAX(fecha_inicio) AS ultima_fecha_inicio
                    FROM intento_simulacro
                    WHERE id_estudiante = :idEstudiante
                        AND fecha_fin IS NOT NULL
                    GROUP BY id_simulacro
                ) ult
                    ON ult.id_simulacro = i1.id_simulacro
                    AND ult.ultima_fecha_inicio = i1.fecha_inicio
                WHERE i1.id_estudiante = :idEstudiante
                    AND i1.fecha_fin IS NOT NULL
            ) i ON i.id_simulacro = s.id_simulacro
            WHERE ge.id_estudiante = :idEstudiante
                AND s.activo = TRUE
                AND CURRENT_DATE BETWEEN p.fecha_inicio AND p.fecha_fin
            ORDER BY s.fecha_creacion DESC, s.id_simulacro DESC
            """, nativeQuery = true)
    List<EstudianteSimulacroProjection> findSimulacrosPeriodoActivoByEstudianteId(
            @Param("idEstudiante") Long idEstudiante
    );
}
