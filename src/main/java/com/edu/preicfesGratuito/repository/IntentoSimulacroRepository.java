package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.IntentoSimulacro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntentoSimulacroRepository extends JpaRepository<IntentoSimulacro, Long> {
}
