package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de localidades geograficas donde operan nodos educativos.
 */
@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
}
