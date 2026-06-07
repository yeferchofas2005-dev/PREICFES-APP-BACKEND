package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Competencia evaluada por una pregunta.
 */
@Entity
@Table(name = "competencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Competencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competencia")
    @EqualsAndHashCode.Include
    private Long idCompetencia;

    @Column(name = "nombre", nullable = false, unique = true, length = 150)
    private String nombre;
}
