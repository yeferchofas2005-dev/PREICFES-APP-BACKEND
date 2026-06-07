package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.SimulacroBanco;
import com.edu.preicfesGratuito.entity.SimulacroBancoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de la relacion simulacro-banco de preguntas.
 *
 * <p>Define que bancos componen un simulacro mediante llave compuesta
 * {@code (id_simulacro, id_banco)}.</p>
 */
@Repository
public interface SimulacroBancoRepository extends JpaRepository<SimulacroBanco, SimulacroBancoId> {
}
