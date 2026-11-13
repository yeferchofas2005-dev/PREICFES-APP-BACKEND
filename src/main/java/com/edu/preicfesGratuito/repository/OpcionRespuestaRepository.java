package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de opciones de respuesta disponibles para cada pregunta.
 */
@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Long> {
    List<OpcionRespuesta> findByPreguntaIdPreguntaIn(List<Long> idsPreguntas);
}
