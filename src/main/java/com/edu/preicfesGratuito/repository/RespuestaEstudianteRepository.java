package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.RespuestaEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de respuestas registradas por estudiantes en un intento.
 */
@Repository
public interface RespuestaEstudianteRepository extends JpaRepository<RespuestaEstudiante, Long> {
}
