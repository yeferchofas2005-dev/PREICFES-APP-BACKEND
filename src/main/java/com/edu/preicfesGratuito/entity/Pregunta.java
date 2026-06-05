package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    @EqualsAndHashCode.Include
    private Long idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco", nullable = false)
    private BancoPregunta banco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caso")
    private CasoPregunta caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia", nullable = false)
    private Competencia competencia;

    @Lob
    @Column(name = "enunciado", nullable = false, columnDefinition = "LONGTEXT")
    private String enunciado;

    @Column(name = "ruta_imagen", length = 500)
    private String rutaImagen;

    @Column(name = "nivel_dificultad")
    private Integer nivelDificultad;

    @Column(name = "orden_caso")
    private Integer ordenCaso;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
}
