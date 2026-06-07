package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio de roles disponibles en el sistema.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por nombre funcional, por ejemplo ADMIN o ESTUDIANTE.
     */
    Optional<Rol> findByNombre(String nombre);
}
