package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.AuthResponseDTO;
import com.edu.preicfesGratuito.dto.LoginRequestDTO;
import com.edu.preicfesGratuito.dto.RolDTO;
import com.edu.preicfesGratuito.dto.UsuarioRequestDTO;
import com.edu.preicfesGratuito.dto.UsuarioResponseDTO;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.mapper.UsuarioMapper;
import com.edu.preicfesGratuito.repository.RolRepository;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioActivo;
    private UsuarioResponseDTO usuarioResponseDTO;

    @BeforeEach
    void setUp() {
        Rol rol = Rol.builder().idRol(1L).nombre("ADMIN").build();
        usuarioActivo = Usuario.builder()
                .idUsuario(1L)
                .rol(rol)
                .nombres("Administrador")
                .apellidos("Sistema")
                .documento("100000001")
                .correo("admin@preicfes.com")
                .passwordHash("$2a$10$hashed")
                .activo(true)
                .build();

        usuarioResponseDTO = UsuarioResponseDTO.builder()
                .idUsuario(1L)
                .nombres("Administrador")
                .apellidos("Sistema")
                .documento("100000001")
                .correo("admin@preicfes.com")
                .activo(true)
                .rol(RolDTO.builder().idRol(1L).nombre("ADMIN").build())
                .build();
    }

    @Test
    void login_cuandoCredencialesValidasYUsuarioActivo_retornaTokenYUsuario() {
        LoginRequestDTO request = new LoginRequestDTO("admin@preicfes.com", "123456");
        UserDetailsImpl userDetails = new UserDetailsImpl(usuarioActivo);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(usuarioRepository.findByCorreo("admin@preicfes.com")).thenReturn(Optional.of(usuarioActivo));
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-demo");
        when(usuarioMapper.toResponseDTO(usuarioActivo)).thenReturn(usuarioResponseDTO);

        AuthResponseDTO response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token-demo");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUsuario().getRol().getNombre()).isEqualTo("ADMIN");
    }

    @Test
    void login_cuandoCredencialesInvalidas_lanzaBadCredentialsException() {
        LoginRequestDTO request = new LoginRequestDTO("admin@preicfes.com", "wrongpass");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_cuandoUsuarioInactivo_lanzaDisabledException() {
        usuarioActivo.setActivo(false);
        LoginRequestDTO request = new LoginRequestDTO("admin@preicfes.com", "123456");
        UserDetailsImpl userDetails = new UserDetailsImpl(usuarioActivo);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(usuarioRepository.findByCorreo("admin@preicfes.com")).thenReturn(Optional.of(usuarioActivo));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("inactivo");
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void register_cuandoDatosValidos_creaUsuarioYRetornaToken() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombres("Nuevo")
                .apellidos("Usuario")
                .documento("99999999")
                .correo("nuevo@preicfes.com")
                .password("123456")
                .idRol(1L)
                .build();

        when(usuarioRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("nuevo@preicfes.com")).thenReturn(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo.getRol()));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActivo);
        when(jwtUtil.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-nuevo");
        when(usuarioMapper.toResponseDTO(usuarioActivo)).thenReturn(usuarioResponseDTO);

        AuthResponseDTO response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-nuevo");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void register_cuandoCorreoDuplicado_lanzaBadRequestException() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .documento("99999999")
                .correo("admin@preicfes.com")
                .build();

        when(usuarioRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("admin@preicfes.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("correo");
    }

    @Test
    void register_cuandoRolNoExiste_lanzaResourceNotFoundException() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .documento("99999999")
                .correo("nuevo@preicfes.com")
                .idRol(99L)
                .build();

        when(usuarioRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("nuevo@preicfes.com")).thenReturn(false);
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
