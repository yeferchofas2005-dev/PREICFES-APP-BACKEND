package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Relacion muchos a muchos entre simulacros y bancos de preguntas.
 */
@Entity
@Table(name = "simulacro_banco")
@IdClass(SimulacroBancoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SimulacroBanco {

    @Id
    @Column(name = "id_simulacro")
    private Long idSimulacro;

    @Id
    @Column(name = "id_banco")
    private Long idBanco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacro", insertable = false, updatable = false)
    private Simulacro simulacro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco", insertable = false, updatable = false)
    private BancoPregunta banco;
}
