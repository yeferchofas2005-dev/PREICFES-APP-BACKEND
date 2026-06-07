package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.GrupoEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de la relacion estudiante-grupo.
 *
 * <p>Cada estudiante pertenece a un unico grupo; la llave primaria es
 * {@code id_estudiante}.</p>
 */
@Repository
public interface GrupoEstudianteRepository extends JpaRepository<GrupoEstudiante, Long> {
}
