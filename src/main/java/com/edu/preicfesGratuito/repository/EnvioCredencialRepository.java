package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.EnvioCredencial;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la cola persistente de envio de credenciales.
 */
@Repository
public interface EnvioCredencialRepository extends JpaRepository<EnvioCredencial, Long> {

    /**
     * Cuenta los correos enviados exitosamente dentro del rango del dia actual (cuota global).
     */
    long countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(
            EstadoEnvioCredencial estado,
            LocalDateTime inicioDia,
            LocalDateTime finDia
    );

    /**
     * Calcula todos los contadores del monitor en una sola consulta agregada.
     */
    @Query(
            value = "SELECT " +
                    "  COUNT(*) AS totalRegistros, " +
                    "  COALESCE(SUM(CASE WHEN e.estado = 'ENVIADO' THEN 1 ELSE 0 END), 0) AS enviados, " +
                    "  COALESCE(SUM(CASE WHEN e.estado = 'PENDIENTE' THEN 1 ELSE 0 END), 0) AS pendientes, " +
                    "  COALESCE(SUM(CASE WHEN e.estado = 'PROCESANDO' THEN 1 ELSE 0 END), 0) AS procesando, " +
                    "  COALESCE(SUM(CASE WHEN e.estado = 'FALLIDO' THEN 1 ELSE 0 END), 0) AS fallidos " +
                    "FROM envio_credencial e " +
                    "INNER JOIN usuario u ON e.id_usuario = u.id_usuario " +
                    "WHERE (:idNodo IS NULL OR u.id_nodo = :idNodo)",
            nativeQuery = true
    )
    EnvioCredencialResumenProjection obtenerResumenAgregado(@Param("idNodo") Long idNodo);

    /**
     * Obtiene el siguiente lote de envios pendientes ordenados por prioridad y orden de llegada.
     */
    @Query("SELECT e FROM EnvioCredencial e " +
           "JOIN FETCH e.usuario u " +
           "JOIN FETCH u.rol " +
           "LEFT JOIN FETCH u.nodo " +
           "WHERE e.estado = com.edu.preicfesGratuito.enums.EstadoEnvioCredencial.PENDIENTE " +
           "AND e.passwordCifradaTemporal IS NOT NULL " +
           "AND u.activo = true " +
           "AND (:idNodo IS NULL OR u.nodo.idNodo = :idNodo) " +
           "ORDER BY e.prioridad ASC, e.idEnvio ASC")
    List<EnvioCredencial> buscarPendientesOrdenados(@Param("idNodo") Long idNodo, Pageable pageable);

    /**
     * Lista registros de la cola paginados y filtrados por nodo y estado opcional.
     */
    @Query(
            value = "SELECT e FROM EnvioCredencial e " +
                    "JOIN FETCH e.usuario u " +
                    "JOIN FETCH u.rol " +
                    "LEFT JOIN FETCH u.nodo n " +
                    "WHERE (:idNodo IS NULL OR n.idNodo = :idNodo) " +
                    "AND (:estado IS NULL OR e.estado = :estado)",
            countQuery = "SELECT COUNT(e) FROM EnvioCredencial e " +
                    "JOIN e.usuario u " +
                    "LEFT JOIN u.nodo n " +
                    "WHERE (:idNodo IS NULL OR n.idNodo = :idNodo) " +
                    "AND (:estado IS NULL OR e.estado = :estado)"
    )
    Page<EnvioCredencial> buscarRegistrosPaginados(
            @Param("idNodo") Long idNodo,
            @Param("estado") EstadoEnvioCredencial estado,
            Pageable pageable
    );

    /**
     * Reencola registros fallidos que aun conservan contrasena temporal cifrada.
     */
    @Modifying
    @Query("UPDATE EnvioCredencial e " +
           "SET e.estado = com.edu.preicfesGratuito.enums.EstadoEnvioCredencial.PENDIENTE, " +
           "    e.fechaProgramada = :fechaHoy, " +
           "    e.ultimoError = null " +
           "WHERE e.estado = com.edu.preicfesGratuito.enums.EstadoEnvioCredencial.FALLIDO " +
           "AND e.passwordCifradaTemporal IS NOT NULL " +
           "AND (:idNodo IS NULL OR e.usuario.idUsuario IN (SELECT u.idUsuario FROM Usuario u WHERE u.nodo.idNodo = :idNodo))")
    int reencolarFallidos(@Param("idNodo") Long idNodo, @Param("fechaHoy") LocalDate fechaHoy);
}
