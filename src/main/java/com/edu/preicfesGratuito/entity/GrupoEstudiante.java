package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "grupo_estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GrupoEstudiante {

    @Id
    @Column(name = "id_estudiante")
    @EqualsAndHashCode.Include
    private Long idEstudiante;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_estudiante",
        referencedColumnName = "id_usuario",
        insertable = false,
        updatable = false
    )
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo", nullable = false)
    private Grupo grupo;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;
}
