package com.edu.preicfesGratuito.entity;

import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cola persistente para el envio de credenciales por correo electronico.
 *
 * <p>Almacena la contrasena cifrada transitoriamente con AES mientras el envio
 * esta pendiente o fallido, y la elimina inmediatamente al marcarse ENVIADO.</p>
 */
@Entity
@Table(name = "envio_credencial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"passwordCifradaTemporal", "usuario"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EnvioCredencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    @EqualsAndHashCode.Include
    private Long idEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoEnvioCredencial estado = EstadoEnvioCredencial.PENDIENTE;

    /**
     * 1 = alta (creacion manual), 2 = normal (carga masiva).
     */
    @Column(name = "prioridad", nullable = false)
    @Builder.Default
    private Integer prioridad = 2;

    @Column(name = "password_cifrada_temporal", columnDefinition = "TEXT")
    private String passwordCifradaTemporal;

    @Column(name = "intentos", nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "ultimo_error", length = 500)
    private String ultimoError;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
