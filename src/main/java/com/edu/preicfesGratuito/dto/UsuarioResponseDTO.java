package com.edu.preicfesGratuito.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombres;
    private String apellidos;
    private String documento;
    private String correo;
    private String telefono;
    private Boolean activo;
    private RolDTO rol;
}
