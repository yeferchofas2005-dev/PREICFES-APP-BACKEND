package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.entity.Localidad;
import com.edu.preicfesGratuito.entity.Nodo;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.mapper.UsuarioMapper;
import com.edu.preicfesGratuito.repository.NodoRepository;
import com.edu.preicfesGratuito.repository.RolRepository;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.service.EnvioCredencialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private NodoRepository nodoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EnvioCredencialService envioCredencialService;

    @Spy
    private UsuarioMapper usuarioMapper = new UsuarioMapper();

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Rol rolAdmin;
    private Rol rolDirectivo;
    private Rol rolProfesor;
    private Rol rolEstudiante;
    private Nodo nodoKennedy;
    private Nodo nodoSuba;
    private Usuario coordinadorKennedy;
    private Usuario estudianteKennedy;

    @BeforeEach
    void setUp() {
        rolAdmin = Rol.builder().idRol(1L).nombre("ADMIN").build();
        rolDirectivo = Rol.builder().idRol(2L).nombre("DIRECTIVO").build();
        rolProfesor = Rol.builder().idRol(3L).nombre("PROFESOR").build();
        rolEstudiante = Rol.builder().idRol(4L).nombre("ESTUDIANTE").build();

        Localidad kennedyLoc = Localidad.builder().idLocalidad(1L).nombre("KENNEDY").build();
        Localidad subaLoc = Localidad.builder().idLocalidad(2L).nombre("SUBA").build();

        nodoKennedy = Nodo.builder().idNodo(1L).nombre("Kennedy Central").localidad(kennedyLoc).activo(true).build();
        nodoSuba = Nodo.builder().idNodo(2L).nombre("Suba Norte").localidad(subaLoc).activo(true).build();

        coordinadorKennedy = Usuario.builder()
                .idUsuario(2L)
                .rol(rolDirectivo)
                .nodo(nodoKennedy)
                .nombres("Carlos")
                .apellidos("Directivo")
                .documento("100000002")
                .correo("directivo@preicfes.com")
                .passwordHash("hash")
                .activo(true)
                .build();

        estudianteKennedy = Usuario.builder()
                .idUsuario(4L)
                .rol(rolEstudiante)
                .nodo(nodoKennedy)
                .nombres("Maria")
                .apellidos("Estudiante")
                .documento("100000004")
                .correo("estudiante@preicfes.com")
                .passwordHash("hash-actual")
                .activo(true)
                .build();
    }

    @Test
    void listarParaAdmin_retornaPaginaMapeada() {
        when(usuarioRepository.buscarConFiltros(eq("%maria%"), isNull(), isNull(), eq(1L), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(estudianteKennedy)));

        PaginaResponseDTO<UsuarioResponseDTO> page = usuarioService.listarParaAdmin("Maria", null, 1L, true, 0, 10);

        assertThat(page.getTotalElementos()).isEqualTo(1);
        assertThat(page.getContenido().get(0).getNodo().getNombre()).isEqualTo("Kennedy Central");
    }

    @Test
    void crearParaAdmin_cuandoRolNoAdminYSinNodo_lanzaBadRequest() {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Ana")
                .apellidos("Profe")
                .documento("20001")
                .correo("ana@preicfes.com")
                .password("123456")
                .idRol(3L)
                .idNodo(null)
                .build();

        when(rolRepository.findById(3L)).thenReturn(Optional.of(rolProfesor));

        assertThatThrownBy(() -> usuarioService.crearParaAdmin(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nodo");
    }

    @Test
    void crearParaAdmin_cuandoValido_cifraPasswordConBCrypt() {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Pedro")
                .apellidos("Admin")
                .documento("20002")
                .correo("PEDRO@preicfes.com")
                .password("Secreta123")
                .idRol(1L)
                .build();

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));
        when(usuarioRepository.existsByDocumento("20002")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("pedro@preicfes.com")).thenReturn(false);
        when(passwordEncoder.encode("Secreta123")).thenReturn("$2a$10$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(99L);
            return u;
        });

        UsuarioResponseDTO res = usuarioService.crearParaAdmin(request);

        assertThat(res.getIdUsuario()).isEqualTo(99L);
        assertThat(res.getCorreo()).isEqualTo("pedro@preicfes.com");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$encoded");
        verifyNoInteractions(envioCredencialService);
    }

    @Test
    void crearParaAdmin_cuandoSolicitaEnvioCorreo_invocaEnvioCredencialService() {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Pedro")
                .apellidos("Admin")
                .documento("20003")
                .correo("pedro2@preicfes.com")
                .password("Secreta123")
                .idRol(1L)
                .enviarCredencialesPorCorreo(true)
                .build();

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));
        when(usuarioRepository.existsByDocumento("20003")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("pedro2@preicfes.com")).thenReturn(false);
        when(passwordEncoder.encode("Secreta123")).thenReturn("$2a$10$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(100L);
            return u;
        });

        usuarioService.crearParaAdmin(request);

        verify(envioCredencialService).registrarEnvioManual(any(Usuario.class), eq("Secreta123"));
    }

    @Test
    void actualizarParaAdmin_cuandoPasswordVacio_conservaPasswordActual() {
        UsuarioActualizarDTO request = UsuarioActualizarDTO.builder()
                .nombres("Maria Actualizada")
                .apellidos("Estudiante")
                .documento("100000004")
                .correo("estudiante@preicfes.com")
                .password("")
                .idRol(4L)
                .idNodo(1L)
                .build();

        when(usuarioRepository.findByIdWithRolAndNodo(4L)).thenReturn(Optional.of(estudianteKennedy));
        when(rolRepository.findById(4L)).thenReturn(Optional.of(rolEstudiante));
        when(nodoRepository.findById(1L)).thenReturn(Optional.of(nodoKennedy));
        when(usuarioRepository.existsByDocumentoAndIdUsuarioNot("100000004", 4L)).thenReturn(false);
        when(usuarioRepository.existsByCorreoAndIdUsuarioNot("estudiante@preicfes.com", 4L)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponseDTO res = usuarioService.actualizarParaAdmin(4L, request);

        assertThat(res.getNombres()).isEqualTo("Maria Actualizada");
        assertThat(estudianteKennedy.getPasswordHash()).isEqualTo("hash-actual");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void cambiarEstadoParaAdmin_cuandoSeAutoInactiva_lanzaBadRequest() {
        when(usuarioRepository.findByIdWithRolAndNodo(1L)).thenReturn(Optional.of(
                Usuario.builder().idUsuario(1L).rol(rolAdmin).activo(true).build()
        ));

        assertThatThrownBy(() -> usuarioService.cambiarEstadoParaAdmin(1L, 1L, new CambiarEstadoDTO(false)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("propio usuario");
    }

    @Test
    void crearParaCoordinador_cuandoIntentaCrearDirectivoOAdmin_lanzaAccessDenied() {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Otro")
                .apellidos("Admin")
                .documento("30001")
                .correo("otro@preicfes.com")
                .password("123456")
                .idRol(1L)
                .build();

        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordinadorKennedy));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));

        assertThatThrownBy(() -> usuarioService.crearParaCoordinador(2L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void actualizarParaCoordinador_cuandoUsuarioEsDeOtroNodo_lanzaAccessDenied() {
        Usuario estudianteSuba = Usuario.builder()
                .idUsuario(55L)
                .rol(rolEstudiante)
                .nodo(nodoSuba)
                .build();
        UsuarioActualizarDTO request = UsuarioActualizarDTO.builder()
                .nombres("X")
                .apellidos("Y")
                .documento("30002")
                .correo("x@preicfes.com")
                .idRol(4L)
                .build();

        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordinadorKennedy));
        when(usuarioRepository.findByIdWithRolAndNodo(55L)).thenReturn(Optional.of(estudianteSuba));

        assertThatThrownBy(() -> usuarioService.actualizarParaCoordinador(2L, 55L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("otro nodo");
    }

    @Test
    void obtenerCatalogosCoordinador_retornaSoloProfesorYEstudianteYSuNodo() {
        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordinadorKennedy));
        when(rolRepository.findAll(any(Sort.class))).thenReturn(List.of(rolAdmin, rolDirectivo, rolProfesor, rolEstudiante));

        UsuarioCatalogosDTO catalogos = usuarioService.obtenerCatalogosCoordinador(2L);

        assertThat(catalogos.getRoles()).extracting(RolDTO::getNombre)
                .containsExactly("PROFESOR", "ESTUDIANTE");
        assertThat(catalogos.getNodos()).extracting(NodoSimpleDTO::getIdNodo)
                .containsExactly(1L);
    }
}
