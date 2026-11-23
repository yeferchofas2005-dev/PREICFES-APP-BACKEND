package com.edu.preicfesGratuito.dto;

import com.edu.preicfesGratuito.util.UsuarioValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(
            regexp = UsuarioValidationPatterns.NOMBRE_PERSONA_REGEX,
            message = "Los nombres solo deben contener letras y espacios"
    )
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    @Pattern(
            regexp = UsuarioValidationPatterns.NOMBRE_PERSONA_REGEX,
            message = "Los apellidos solo deben contener letras y espacios"
    )
    private String apellidos;

    @NotBlank(message = "El documento es requerido")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    @Pattern(
            regexp = UsuarioValidationPatterns.DOCUMENTO_REGEX,
            message = "El documento solo debe contener entre 5 y 20 dígitos numéricos"
    )
    private String documento;

    @NotBlank(message = "El correo es requerido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    @Pattern(
            regexp = UsuarioValidationPatterns.CORREO_REGEX,
            message = "Formato de correo electrónico no válido"
    )
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Pattern(
            regexp = UsuarioValidationPatterns.TELEFONO_OPCIONAL_REGEX,
            message = "El teléfono solo debe contener números (7 a 15 dígitos, opcional '+' inicial)"
    )
    private String telefono;

    @NotNull(message = "El rol es requerido")
    private Long idRol;

    private Long idNodo;

    private Boolean activo;

    private Boolean enviarCredencialesPorCorreo;
}
