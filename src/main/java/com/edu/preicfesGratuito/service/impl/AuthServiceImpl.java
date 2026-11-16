package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.AuthResponseDTO;
import com.edu.preicfesGratuito.dto.LoginRequestDTO;
import com.edu.preicfesGratuito.dto.UsuarioRequestDTO;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.RolNombre;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.mapper.UsuarioMapper;
import com.edu.preicfesGratuito.repository.RolRepository;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.AuthService;
import com.edu.preicfesGratuito.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del flujo de autenticacion.
 *
 * <p>Centraliza la validacion de credenciales, el cifrado de contrasenas,
 * la creacion de usuarios y la emision de tokens JWT.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;

    @Value("${app.auth.self-register-enabled:false}")
    private boolean selfRegisterEnabled;

    /**
     * Autentica al usuario usando Spring Security y retorna un JWT si las credenciales son validas.
     *
     * @param request correo y contrasena recibidos desde el cliente
     * @return respuesta de autenticacion con token y datos del usuario
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByCorreo(userDetails.getCorreo())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("El usuario se encuentra inactivo");
        }

        return buildAuthResponse(userDetails, usuario);
    }

    /**
     * Registra un usuario nuevo validando unicidad de documento y correo.
     *
     * <p>La contrasena nunca se persiste en texto plano; se almacena como hash BCrypt
     * en la columna {@code password_hash}.</p>
     *
     * @param request datos enviados para crear el usuario
     * @return respuesta de autenticacion con token y usuario creado
     */
    @Override
    @Transactional
    public AuthResponseDTO register(UsuarioRequestDTO request) {
        if (!selfRegisterEnabled) {
            throw new AccessDeniedException(
                    "El autorregistro público está deshabilitado. Solicita tu cuenta a un administrador o coordinador."
            );
        }

        if (usuarioRepository.existsByDocumento(request.getDocumento())) {
            throw new BadRequestException("Ya existe un usuario con ese documento");
        }

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BadRequestException("Ya existe un usuario con ese correo");
        }

        Rol rolEstudiante = rolRepository.findByNombre(RolNombre.ESTUDIANTE.name())
                .orElseThrow(() -> new ResourceNotFoundException("Rol ESTUDIANTE no encontrado"));

        if (request.getIdRol() != null && !rolEstudiante.getIdRol().equals(request.getIdRol())) {
            throw new AccessDeniedException("El registro público solo permite crear cuentas de rol ESTUDIANTE");
        }

        Usuario usuario = Usuario.builder()
                .rol(rolEstudiante)
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .documento(request.getDocumento())
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        UserDetailsImpl userDetails = new UserDetailsImpl(usuarioGuardado);

        return buildAuthResponse(userDetails, usuarioGuardado);
    }

    /**
     * Construye la respuesta comun de login y registro.
     *
     * @param userDetails principal autenticable usado para firmar el token
     * @param usuario entidad del usuario que se expondra como DTO
     * @return respuesta con token Bearer y datos publicos del usuario
     */
    private AuthResponseDTO buildAuthResponse(UserDetailsImpl userDetails, Usuario usuario) {
        return AuthResponseDTO.builder()
                .token(jwtUtil.generateToken(userDetails))
                .tokenType("Bearer")
                .usuario(usuarioMapper.toResponseDTO(usuario))
                .build();
    }
}
