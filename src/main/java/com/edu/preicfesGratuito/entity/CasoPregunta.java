package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "caso_pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CasoPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caso")
    @EqualsAndHashCode.Include
    private Long idCaso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco", nullable = false)
    private BancoPregunta banco;

    @Column(name = "titulo", length = 255)
    private String titulo;

    @Lob
    @Column(name = "texto_base", columnDefinition = "LONGTEXT")
    private String textoBase;

    @Column(name = "ruta_imagen", length = 500)
    private String rutaImagen;
}
