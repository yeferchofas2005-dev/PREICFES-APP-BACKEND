package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.EstadisticaGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadisticaGrupoRepository extends JpaRepository<EstadisticaGrupo, Long> {
}
