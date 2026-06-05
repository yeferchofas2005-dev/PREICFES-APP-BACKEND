package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nodo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Nodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nodo")
    @EqualsAndHashCode.Include
    private Long idNodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_localidad", nullable = false)
    private Localidad localidad;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
}
