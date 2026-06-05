package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.CasoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CasoPreguntaRepository extends JpaRepository<CasoPregunta, Long> {
}
