package com.edu.preicfesGratuito.entity;

import com.edu.preicfesGratuito.enums.TipoParametroGeneral;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que almacena los parametros generales configurables de la plataforma.
 */
@Entity
@Table(name = "parametro_general")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParametroGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    @EqualsAndHashCode.Include
    private Long idParametro;

    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @Column(name = "valor", nullable = false, length = 255)
    private String valor;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoParametroGeneral tipo;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
