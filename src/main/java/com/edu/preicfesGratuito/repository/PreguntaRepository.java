package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de preguntas evaluativas pertenecientes a un banco.
 */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    @Query("SELECT DISTINCT p FROM Pregunta p " +
           "JOIN fetch p.banco b " +
           "LEFT JOIN fetch p.caso c " +
           "WHERE b.idBanco IN (SELECT sb.idBanco FROM SimulacroBanco sb WHERE sb.idSimulacro = :idSimulacro) " +
           "AND b.area.idArea = :idArea " +
           "AND p.activo = true " +
           "ORDER BY p.ordenCaso ASC, p.idPregunta ASC")
    List<Pregunta> findPreguntasBySimulacroAndArea(@Param("idSimulacro") Long idSimulacro, @Param("idArea") Long idArea);
}
