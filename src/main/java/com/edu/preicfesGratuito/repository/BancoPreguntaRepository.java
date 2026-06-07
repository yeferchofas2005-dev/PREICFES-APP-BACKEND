package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.BancoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de bancos de preguntas asociados a un periodo y area.
 */
@Repository
public interface BancoPreguntaRepository extends JpaRepository<BancoPregunta, Long> {
}
