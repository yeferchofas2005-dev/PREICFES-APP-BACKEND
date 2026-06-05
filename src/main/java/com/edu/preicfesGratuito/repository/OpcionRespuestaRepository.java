package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Long> {
}
