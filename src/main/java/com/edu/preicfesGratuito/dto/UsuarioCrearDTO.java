package com.edu.preicfesGratuito.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Datos de entrada para crear un usuario desde administracion o coordinacion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCrearDTO {

    @NotBlank(message = "Los nombres son requeridos")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El documento es requerido")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    private String documento;

    @Email(message = "Formato de correo no válido")
    @NotBlank(message = "El correo es requerido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    @NotNull(message = "El rol es requerido")
    private Long idRol;

    private Long idNodo;

    private Boolean activo;
}
