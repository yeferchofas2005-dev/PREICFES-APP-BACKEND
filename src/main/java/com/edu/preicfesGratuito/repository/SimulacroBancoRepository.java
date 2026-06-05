package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.SimulacroBanco;
import com.edu.preicfesGratuito.entity.SimulacroBancoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulacroBancoRepository extends JpaRepository<SimulacroBanco, SimulacroBancoId> {
}
