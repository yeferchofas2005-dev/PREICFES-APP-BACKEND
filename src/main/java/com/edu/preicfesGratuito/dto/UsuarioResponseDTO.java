package com.edu.preicfesGratuito.dto;

import lombok.*;

/**
 * Representacion publica de un usuario.
 *
 * <p>No expone el hash de contrasena ni otros datos sensibles.</p>
 */
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
    private NodoSimpleDTO nodo;
}
