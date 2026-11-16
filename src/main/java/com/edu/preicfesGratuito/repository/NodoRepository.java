package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Nodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de nodos o sedes operativas asociadas a una localidad.
 */
@Repository
public interface NodoRepository extends JpaRepository<Nodo, Long> {

    /**
     * Lista nodos activos con su localidad cargada para evitar consultas N+1.
     */
    @Query("SELECT n FROM Nodo n JOIN FETCH n.localidad WHERE n.activo = true ORDER BY n.nombre ASC")
    List<Nodo> findAllActivosWithLocalidad();
}
