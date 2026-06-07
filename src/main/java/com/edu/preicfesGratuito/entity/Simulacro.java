package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Simulacro asignable a grupos de estudiantes.
 *
 * <p>Mapea la tabla {@code simulacro}. Se compone de bancos de preguntas
 * y pertenece a un periodo academico. El tiempo limite define la duracion
 * maxima permitida para resolverlo.</p>
 */
@Entity
@Table(name = "simulacro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Simulacro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacro")
    @EqualsAndHashCode.Include
    private Long idSimulacro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodoAcademico periodo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tiempo_limite_minutos", nullable = false)
    private Integer tiempoLimiteMinutos;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
