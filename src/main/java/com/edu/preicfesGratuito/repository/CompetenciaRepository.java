package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Competencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de competencias evaluadas por las preguntas.
 */
@Repository
public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {
}
