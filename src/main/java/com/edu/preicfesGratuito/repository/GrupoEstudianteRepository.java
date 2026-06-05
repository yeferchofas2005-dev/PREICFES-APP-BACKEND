package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.GrupoEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoEstudianteRepository extends JpaRepository<GrupoEstudiante, Long> {
}
