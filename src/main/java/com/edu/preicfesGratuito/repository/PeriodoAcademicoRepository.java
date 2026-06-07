package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de periodos academicos que delimitan bancos y simulacros.
 */
@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
}
