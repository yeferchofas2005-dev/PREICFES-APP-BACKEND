package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de preguntas evaluativas pertenecientes a un banco.
 */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
}
