package com.edu.preicfesGratuito.dto;

import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista publica de un registro en la cola de envio de credenciales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvioCredencialItemDTO {
    private Long idEnvio;
    private Long idUsuario;
    private String nombreCompleto;
    private String documento;
    private String correo;
    private String rol;
    private String nodo;
    private EstadoEnvioCredencial estado;
    private Integer prioridad;
    private Integer intentos;
    private String ultimoError;
    private LocalDate fechaProgramada;
    private LocalDateTime fechaEnvio;
}
