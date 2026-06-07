package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Nodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de nodos o sedes operativas asociadas a una localidad.
 */
@Repository
public interface NodoRepository extends JpaRepository<Nodo, Long> {
}
