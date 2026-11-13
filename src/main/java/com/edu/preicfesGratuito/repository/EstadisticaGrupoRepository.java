package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.EstadisticaGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de estadisticas consolidadas por grupo academico.
 */
@Repository
public interface EstadisticaGrupoRepository extends JpaRepository<EstadisticaGrupo, Long> {

    @Procedure(procedureName = "sp_actualizar_estadistica_grupo")
    void spActualizarEstadisticaGrupo(@Param("p_id_grupo") Long pIdGrupo);
}
