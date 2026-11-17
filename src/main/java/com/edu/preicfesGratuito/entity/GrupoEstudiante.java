package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;

/**
 * Relacion entre un estudiante y su grupo academico.
 *
 * <p>La tabla usa {@code id_estudiante} como llave primaria para garantizar
 * que un estudiante pertenezca a un solo grupo.</p>
 */
@Entity
@Table(name = "grupo_estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GrupoEstudiante implements Persistable<Long> {

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

    @Transient
    @Builder.Default
    private boolean nuevo = true;

    @Override
    public Long getId() {
        return idEstudiante;
    }

    @Override
    public boolean isNew() {
        return nuevo;
    }

    @PostLoad
    @PostPersist
    void marcarPersistido() {
        this.nuevo = false;
    }
}
