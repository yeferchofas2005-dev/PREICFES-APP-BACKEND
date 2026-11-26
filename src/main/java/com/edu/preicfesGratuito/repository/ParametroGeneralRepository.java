package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.ParametroGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la gestion de parametros generales del sistema.
 */
public interface ParametroGeneralRepository extends JpaRepository<ParametroGeneral, Long> {

    Optional<ParametroGeneral> findByClave(String clave);

    List<ParametroGeneral> findAllByOrderByClaveAsc();
}
