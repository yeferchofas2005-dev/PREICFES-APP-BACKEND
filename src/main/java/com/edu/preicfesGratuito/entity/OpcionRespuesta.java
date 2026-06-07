package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Opcion de respuesta asociada a una pregunta.
 */
@Entity
@Table(name = "opcion_respuesta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    @EqualsAndHashCode.Include
    private Long idOpcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    @Lob
    @Column(name = "texto", nullable = false, columnDefinition = "LONGTEXT")
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;
}
