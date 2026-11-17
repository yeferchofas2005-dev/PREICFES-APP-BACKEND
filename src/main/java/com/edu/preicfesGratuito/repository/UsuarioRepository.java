package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol LEFT JOIN FETCH u.nodo WHERE u.correo = :correo")
    Optional<Usuario> findByCorreo(@Param("correo") String correo);

    /**
     * Verifica si ya existe un usuario con el documento indicado.
     */
    boolean existsByDocumento(String documento);

    /**
     * Verifica si ya existe un usuario con el correo indicado.
     */
    boolean existsByCorreo(String correo);

    /**
     * Verifica unicidad de documento excluyendo al propio usuario en edicion.
     */
    boolean existsByDocumentoAndIdUsuarioNot(String documento, Long idUsuario);

    /**
     * Verifica unicidad de correo excluyendo al propio usuario en edicion.
     */
    boolean existsByCorreoAndIdUsuarioNot(String correo, Long idUsuario);

    /**
     * Busca un usuario por ID trayendo rol, nodo y localidad en una sola consulta.
     */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol LEFT JOIN FETCH u.nodo n LEFT JOIN FETCH n.localidad WHERE u.idUsuario = :idUsuario")
    Optional<Usuario> findByIdWithRolAndNodo(@Param("idUsuario") Long idUsuario);

    /**
     * Consulta paginada con filtros dinamicos y cero N+1 queries.
     */
    @Query(
            value = "SELECT u FROM Usuario u " +
                    "JOIN FETCH u.rol r " +
                    "LEFT JOIN FETCH u.nodo n " +
                    "LEFT JOIN FETCH n.localidad " +
                    "WHERE (:busqueda IS NULL OR LOWER(u.nombres) LIKE :busqueda " +
                    "   OR LOWER(u.apellidos) LIKE :busqueda " +
                    "   OR LOWER(u.documento) LIKE :busqueda " +
                    "   OR LOWER(u.correo) LIKE :busqueda) " +
                    "AND (:idRol IS NULL OR r.idRol = :idRol) " +
                    "AND (:rolesPermitidos IS NULL OR r.nombre IN :rolesPermitidos) " +
                    "AND (:idNodo IS NULL OR n.idNodo = :idNodo) " +
                    "AND (:activo IS NULL OR u.activo = :activo)",
            countQuery = "SELECT COUNT(u) FROM Usuario u " +
                    "JOIN u.rol r " +
                    "LEFT JOIN u.nodo n " +
                    "WHERE (:busqueda IS NULL OR LOWER(u.nombres) LIKE :busqueda " +
                    "   OR LOWER(u.apellidos) LIKE :busqueda " +
                    "   OR LOWER(u.documento) LIKE :busqueda " +
                    "   OR LOWER(u.correo) LIKE :busqueda) " +
                    "AND (:idRol IS NULL OR r.idRol = :idRol) " +
                    "AND (:rolesPermitidos IS NULL OR r.nombre IN :rolesPermitidos) " +
                    "AND (:idNodo IS NULL OR n.idNodo = :idNodo) " +
                    "AND (:activo IS NULL OR u.activo = :activo)"
    )
    Page<Usuario> buscarConFiltros(
            @Param("busqueda") String busqueda,
            @Param("idRol") Long idRol,
            @Param("rolesPermitidos") List<String> rolesPermitidos,
            @Param("idNodo") Long idNodo,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
    /**
     * Consulta documentos ya existentes en BD para validacion en lote.
     */
    @Query("SELECT u.documento FROM Usuario u WHERE u.documento IN :documentos")
    List<String> findDocumentosExistentes(@Param("documentos") Collection<String> documentos);

    /**
     * Consulta correos (en minusculas) ya existentes en BD para validacion en lote.
     */
    @Query("SELECT LOWER(u.correo) FROM Usuario u WHERE LOWER(u.correo) IN :correos")
    List<String> findCorreosExistentes(@Param("correos") Collection<String> correos);
}
