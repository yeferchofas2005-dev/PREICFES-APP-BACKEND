package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Area;
import com.edu.preicfesGratuito.dto.AreaExamenDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de areas evaluativas (asignaturas ICFES).
 */
@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {

    @Query("SELECT new com.edu.preicfesGratuito.dto.AreaExamenDTO(a.idArea, a.nombre, CAST(COUNT(p.idPregunta) AS int)) " +
           "FROM SimulacroBanco sb " +
           "JOIN sb.banco b " +
           "JOIN b.area a " +
           "JOIN Pregunta p ON p.banco.idBanco = b.idBanco " +
           "WHERE sb.idSimulacro = :idSimulacro AND p.activo = true AND b.activo = true " +
           "GROUP BY a.idArea, a.nombre")
    List<AreaExamenDTO> findAreasBySimulacro(@Param("idSimulacro") Long idSimulacro);
}
