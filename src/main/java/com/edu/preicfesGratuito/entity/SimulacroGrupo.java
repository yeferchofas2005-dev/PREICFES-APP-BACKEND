package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "simulacro_grupo")
@IdClass(SimulacroGrupoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SimulacroGrupo {

    @Id
    @Column(name = "id_simulacro")
    private Long idSimulacro;

    @Id
    @Column(name = "id_grupo")
    private Long idGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacro", insertable = false, updatable = false)
    private Simulacro simulacro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", insertable = false, updatable = false)
    private Grupo grupo;
}
