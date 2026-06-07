package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Area evaluada en el simulacro, equivalente a una asignatura ICFES.
 */
@Entity
@Table(name = "area")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area")
    @EqualsAndHashCode.Include
    private Long idArea;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
}
