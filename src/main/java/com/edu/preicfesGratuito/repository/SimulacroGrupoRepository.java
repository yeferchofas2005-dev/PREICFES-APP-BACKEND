package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.SimulacroGrupo;
import com.edu.preicfesGratuito.entity.SimulacroGrupoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulacroGrupoRepository extends JpaRepository<SimulacroGrupo, SimulacroGrupoId> {
}
