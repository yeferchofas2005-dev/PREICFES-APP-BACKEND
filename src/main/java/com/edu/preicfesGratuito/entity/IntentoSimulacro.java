package com.edu.preicfesGratuito.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Intento realizado por un estudiante sobre un simulacro.
 *
 * <p>Guarda los resultados finales usados por resumenes y estadisticas.</p>
 */
@Entity
@Table(name = "intento_simulacro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IntentoSimulacro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    @EqualsAndHashCode.Include
    private Long idIntento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Usuario estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacro", nullable = false)
    private Simulacro simulacro;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "puntaje_total", precision = 10, scale = 2)
    private BigDecimal puntajeTotal;

    @Column(name = "porcentaje_aciertos", precision = 5, scale = 2)
    private BigDecimal porcentajeAciertos;

    @Column(name = "tiempo_total_segundos")
    private Integer tiempoTotalSegundos;
}
