package com.edu.preicfesGratuito.dto;

import com.edu.preicfesGratuito.enums.TipoParametroGeneral;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con el detalle de un parametro general del sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroGeneralResponseDTO {

    private Long idParametro;
    private String clave;
    private String valor;
    private String descripcion;
    private TipoParametroGeneral tipo;
    private LocalDateTime fechaActualizacion;
}
