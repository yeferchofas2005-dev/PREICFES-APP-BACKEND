package com.edu.preicfesGratuito.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para actualizar el valor y descripcion de un parametro general.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroGeneralActualizarDTO {

    @NotBlank(message = "El valor del parámetro es obligatorio")
    @Size(max = 255, message = "El valor no puede superar los 255 caracteres")
    private String valor;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;
}
