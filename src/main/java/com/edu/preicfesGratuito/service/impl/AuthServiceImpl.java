package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.AuthResponseDTO;
import com.edu.preicfesGratuito.dto.LoginRequestDTO;
import com.edu.preicfesGratuito.dto.UsuarioRequestDTO;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.mapper.UsuarioMapper;
import com.edu.preicfesGratuito.repository.RolRepository;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.AuthService;
import com.edu.preicfesGratuito.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;

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

    @Override
    @Transactional
    public AuthResponseDTO register(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByDocumento(request.getDocumento())) {
            throw new BadRequestException("Ya existe un usuario con ese documento");
        }

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BadRequestException("Ya existe un usuario con ese correo");
        }

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + request.getIdRol()));

        Usuario usuario = Usuario.builder()
                .rol(rol)
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

    private AuthResponseDTO buildAuthResponse(UserDetailsImpl userDetails, Usuario usuario) {
        return AuthResponseDTO.builder()
                .token(jwtUtil.generateToken(userDetails))
                .tokenType("Bearer")
                .usuario(usuarioMapper.toResponseDTO(usuario))
                .build();
    }
}
