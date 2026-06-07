package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.SimulacroGrupo;
import com.edu.preicfesGratuito.entity.SimulacroGrupoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de la relacion simulacro-grupo.
 *
 * <p>Asigna simulacros a grupos de estudiantes mediante llave compuesta
 * {@code (id_simulacro, id_grupo)}.</p>
 */
@Repository
public interface SimulacroGrupoRepository extends JpaRepository<SimulacroGrupo, SimulacroGrupoId> {
}
