package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "localidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Localidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_localidad")
    @EqualsAndHashCode.Include
    private Long idLocalidad;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
}
