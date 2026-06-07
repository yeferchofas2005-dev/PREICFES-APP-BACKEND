package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla de apoyo con estadisticas consolidadas por grupo.
 */
@Entity
@Table(name = "estadistica_grupo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EstadisticaGrupo {

    @Id
    @Column(name = "id_grupo")
    @EqualsAndHashCode.Include
    private Long idGrupo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_grupo",
        referencedColumnName = "id_grupo",
        insertable = false,
        updatable = false
    )
    private Grupo grupo;

    @Column(name = "total_simulacros")
    @Builder.Default
    private Integer totalSimulacros = 0;

    @Column(name = "promedio_general", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal promedioGeneral = BigDecimal.ZERO;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
}
