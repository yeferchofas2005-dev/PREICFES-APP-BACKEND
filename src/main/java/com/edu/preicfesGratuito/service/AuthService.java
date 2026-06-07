package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.AuthResponseDTO;
import com.edu.preicfesGratuito.dto.LoginRequestDTO;
import com.edu.preicfesGratuito.dto.UsuarioRequestDTO;

/**
 * Contrato de negocio para autenticacion y registro de usuarios.
 */
public interface AuthService {

    /**
     * Valida credenciales y genera un token JWT.
     *
     * @param request correo y contrasena del usuario
     * @return token y datos del usuario autenticado
     */
    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Crea un usuario nuevo con contrasena cifrada y genera un token JWT.
     *
     * @param request datos del usuario a registrar
     * @return token y datos del usuario creado
     */
    AuthResponseDTO register(UsuarioRequestDTO request);
}
