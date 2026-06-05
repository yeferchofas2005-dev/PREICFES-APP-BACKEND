package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Simulacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulacroRepository extends JpaRepository<Simulacro, Long> {
}
