package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio de usuarios del sistema.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por documento unico.
     */
    Optional<Usuario> findByDocumento(String documento);

    /**
     * Busca un usuario por correo, usado en login y carga de JWT.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Verifica si ya existe un usuario con el documento indicado.
     */
    boolean existsByDocumento(String documento);

    /**
     * Verifica si ya existe un usuario con el correo indicado.
     */
    boolean existsByCorreo(String correo);
}
