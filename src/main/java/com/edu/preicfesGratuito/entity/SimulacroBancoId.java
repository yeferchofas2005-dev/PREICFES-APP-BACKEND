package com.edu.preicfesGratuito.entity;

import java.io.Serializable;
import lombok.*;

/**
 * Llave compuesta para la relacion {@link SimulacroBanco}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacroBancoId implements Serializable {
    private Long idSimulacro;
    private Long idBanco;
}
