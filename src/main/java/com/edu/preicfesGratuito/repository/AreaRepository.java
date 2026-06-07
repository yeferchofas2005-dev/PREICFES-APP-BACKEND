package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de areas evaluativas (asignaturas ICFES).
 */
@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
}
