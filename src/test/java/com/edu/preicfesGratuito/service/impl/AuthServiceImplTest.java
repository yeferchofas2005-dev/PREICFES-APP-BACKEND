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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
    private Rol rolEstudiante;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = Rol.builder().idRol(1L).nombre("ADMIN").build();
        rolEstudiante = Rol.builder().idRol(4L).nombre("ESTUDIANTE").build();

        usuarioActivo = Usuario.builder()
                .idUsuario(1L)
                .rol(rolAdmin)
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
    void register_cuandoAutorregistroDeshabilitado_lanzaAccessDeniedException() {
        ReflectionTestUtils.setField(authService, "selfRegisterEnabled", false);
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .documento("99999999")
                .correo("nuevo@preicfes.com")
                .idRol(4L)
                .build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void register_cuandoAutorregistroHabilitadoYRolEstudiante_creaUsuario() {
        ReflectionTestUtils.setField(authService, "selfRegisterEnabled", true);
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombres("Nuevo")
                .apellidos("Estudiante")
                .documento("99999999")
                .correo("nuevo@preicfes.com")
                .password("123456")
                .idRol(4L)
                .build();

        Usuario estudianteGuardado = Usuario.builder()
                .idUsuario(10L)
                .rol(rolEstudiante)
                .correo("nuevo@preicfes.com")
                .passwordHash("$2a$10$encoded")
                .activo(true)
                .build();
        UsuarioResponseDTO estudianteDTO = UsuarioResponseDTO.builder()
                .idUsuario(10L)
                .correo("nuevo@preicfes.com")
                .rol(RolDTO.builder().idRol(4L).nombre("ESTUDIANTE").build())
                .build();

        when(usuarioRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("nuevo@preicfes.com")).thenReturn(false);
        when(rolRepository.findByNombre("ESTUDIANTE")).thenReturn(Optional.of(rolEstudiante));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(estudianteGuardado);
        when(jwtUtil.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-nuevo");
        when(usuarioMapper.toResponseDTO(estudianteGuardado)).thenReturn(estudianteDTO);

        AuthResponseDTO response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-nuevo");
        assertThat(response.getUsuario().getRol().getNombre()).isEqualTo("ESTUDIANTE");
    }

    @Test
    void register_cuandoAutorregistroHabilitadoEIntentaRolPrivilegiado_lanzaAccessDeniedException() {
        ReflectionTestUtils.setField(authService, "selfRegisterEnabled", true);
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .documento("99999999")
                .correo("atacante@preicfes.com")
                .idRol(1L)
                .build();

        when(usuarioRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("atacante@preicfes.com")).thenReturn(false);
        when(rolRepository.findByNombre("ESTUDIANTE")).thenReturn(Optional.of(rolEstudiante));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ESTUDIANTE");
    }

    @Test
    void register_cuandoCorreoDuplicado_lanzaBadRequestException() {
        ReflectionTestUtils.setField(authService, "selfRegisterEnabled", true);
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
    void register_cuandoRolEstudianteNoExiste_lanzaResourceNotFoundException() {
        ReflectionTestUtils.setField(authService, "selfRegisterEnabled", true);
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .documento("99999999")
                .correo("nuevo@preicfes.com")
                .idRol(4L)
                .build();

        when(usuarioRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("nuevo@preicfes.com")).thenReturn(false);
        when(rolRepository.findByNombre("ESTUDIANTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
