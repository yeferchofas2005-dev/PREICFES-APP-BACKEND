package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.exception.GlobalExceptionHandler;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.CargaMasivaUsuarioService;
import com.edu.preicfesGratuito.service.ExportacionUsuarioService;
import com.edu.preicfesGratuito.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CoordinadorUsuarioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CargaMasivaUsuarioService cargaMasivaUsuarioService;

    @Mock
    private ExportacionUsuarioService exportacionUsuarioService;

    @InjectMocks
    private CoordinadorUsuarioController coordinadorUsuarioController;

    @BeforeEach
    void setUp() {
        UserDetailsImpl principal = new UserDetailsImpl(Usuario.builder()
                .idUsuario(2L)
                .correo("directivo@preicfes.com")
                .passwordHash("hash")
                .activo(true)
                .rol(Rol.builder().idRol(2L).nombre("DIRECTIVO").build())
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc = MockMvcBuilders.standaloneSetup(coordinadorUsuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listar_usaIdCoordinadorAutenticadoYRetorna200() throws Exception {
        when(usuarioService.listarParaCoordinador(eq(2L), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(PaginaResponseDTO.<UsuarioResponseDTO>builder()
                        .contenido(List.of())
                        .paginaActual(0)
                        .tamanoPagina(10)
                        .totalElementos(0)
                        .totalPaginas(0)
                        .ultima(true)
                        .build());

        mockMvc.perform(get("/api/directivo/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginaActual").value(0));
    }

    @Test
    void crear_cuandoRolNoPermitido_retorna403() throws Exception {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Test")
                .apellidos("Admin")
                .documento("998877")
                .correo("test@preicfes.com")
                .password("123456")
                .idRol(1L)
                .build();

        when(usuarioService.crearParaCoordinador(eq(2L), any()))
                .thenThrow(new AccessDeniedException("Un coordinador solo puede gestionar usuarios con rol PROFESOR o ESTUDIANTE"));

        mockMvc.perform(post("/api/directivo/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void cambiarEstado_retorna200() throws Exception {
        when(usuarioService.cambiarEstadoParaCoordinador(eq(2L), eq(4L), any(CambiarEstadoDTO.class)))
                .thenReturn(UsuarioResponseDTO.builder().idUsuario(4L).activo(false).build());

        mockMvc.perform(patch("/api/directivo/usuarios/4/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CambiarEstadoDTO(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void cargarMasiva_usaIdCoordinadorAutenticadoYRetorna200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("archivo", "est.xlsx", "application/octet-stream", new byte[]{1, 2});
        when(cargaMasivaUsuarioService.cargarEstudiantesDesdeExcel(eq(2L), isNull(), any(), eq(false)))
                .thenReturn(CargaMasivaUsuariosResponseDTO.builder()
                        .totalFilasProcesadas(1)
                        .exitosos(1)
                        .fallidos(0)
                        .vinculadosAGrupo(1)
                        .errores(List.of())
                        .build());

        mockMvc.perform(multipart("/api/directivo/usuarios/carga-masiva").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitosos").value(1));
    }

    @Test
    void exportar_retornaExcel200ParaCoordinador() throws Exception {
        org.mockito.Mockito.doAnswer(inv -> {
            java.io.OutputStream os = inv.getArgument(4);
            os.write(new byte[]{1, 2, 3});
            return null;
        }).when(exportacionUsuarioService).exportarParaCoordinador(eq(2L), any(), any(), any(), any());

        mockMvc.perform(get("/api/directivo/usuarios/exportar"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString("usuarios_nodo.xlsx")));
    }
}
