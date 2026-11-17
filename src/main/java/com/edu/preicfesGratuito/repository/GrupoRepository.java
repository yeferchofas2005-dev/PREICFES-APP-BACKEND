package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de grupos academicos dirigidos por un profesor en un nodo.
 */
@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    /**
     * Lista los grupos activos de un nodo para vincular estudiantes por nombre.
     */
    List<Grupo> findByNodoIdNodoAndActivoTrue(Long idNodo);
}
