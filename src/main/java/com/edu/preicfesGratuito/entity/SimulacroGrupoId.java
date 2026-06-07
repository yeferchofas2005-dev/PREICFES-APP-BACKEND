package com.edu.preicfesGratuito.entity;

import java.io.Serializable;
import lombok.*;

/**
 * Llave compuesta para la relacion {@link SimulacroGrupo}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacroGrupoId implements Serializable {
    private Long idSimulacro;
    private Long idGrupo;
}
