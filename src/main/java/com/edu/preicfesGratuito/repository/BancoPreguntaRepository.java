package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.BancoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BancoPreguntaRepository extends JpaRepository<BancoPregunta, Long> {
}
