package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Respuesta seleccionada por un estudiante para una pregunta en un intento.
 *
 * <p>La base de datos calcula {@code es_correcta} mediante trigger al insertar
 * la respuesta.</p>
 */
@Entity
@Table(
    name = "respuesta_estudiante",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_respuesta_intento_pregunta",
            columnNames = {"id_intento", "id_pregunta"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RespuestaEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    @EqualsAndHashCode.Include
    private Long idRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intento", nullable = false)
    private IntentoSimulacro intento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion", nullable = false)
    private OpcionRespuesta opcion;

    @Column(name = "es_correcta")
    private Boolean esCorrecta;

    @Column(name = "tiempo_respuesta_segundos")
    private Integer tiempoRespuestaSegundos;
}
