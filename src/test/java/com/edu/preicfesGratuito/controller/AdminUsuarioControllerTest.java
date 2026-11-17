package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.GlobalExceptionHandler;
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
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CargaMasivaUsuarioService cargaMasivaUsuarioService;

    @Mock
    private ExportacionUsuarioService exportacionUsuarioService;

    @InjectMocks
    private AdminUsuarioController adminUsuarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUsuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void listar_retornaPagina200() throws Exception {
        PaginaResponseDTO<UsuarioResponseDTO> pagina = PaginaResponseDTO.<UsuarioResponseDTO>builder()
                .contenido(List.of(UsuarioResponseDTO.builder()
                        .idUsuario(1L)
                        .nombres("Admin")
                        .correo("admin@preicfes.com")
                        .build()))
                .paginaActual(0)
                .tamanoPagina(10)
                .totalElementos(1)
                .totalPaginas(1)
                .ultima(true)
                .build();

        when(usuarioService.listarParaAdmin(any(), any(), any(), any(), eq(0), eq(10))).thenReturn(pagina);

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].correo").value("admin@preicfes.com"));
    }

    @Test
    void crear_cuandoDatosValidos_retorna201() throws Exception {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Laura")
                .apellidos("Gomez")
                .documento("12345678")
                .correo("laura@preicfes.com")
                .password("123456")
                .idRol(3L)
                .idNodo(1L)
                .build();

        UsuarioResponseDTO respuesta = UsuarioResponseDTO.builder()
                .idUsuario(10L)
                .nombres("Laura")
                .correo("laura@preicfes.com")
                .activo(true)
                .build();

        when(usuarioService.crearParaAdmin(any(UsuarioCrearDTO.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(10));
    }

    @Test
    void crear_cuandoCorreoDuplicado_retorna400() throws Exception {
        UsuarioCrearDTO request = UsuarioCrearDTO.builder()
                .nombres("Laura")
                .apellidos("Gomez")
                .documento("12345678")
                .correo("duplicado@preicfes.com")
                .password("123456")
                .idRol(3L)
                .idNodo(1L)
                .build();

        when(usuarioService.crearParaAdmin(any())).thenThrow(new BadRequestException("Ya existe un usuario con ese correo"));

        mockMvc.perform(post("/api/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya existe un usuario con ese correo"));
    }

    @Test
    void obtenerCatalogos_retorna200() throws Exception {
        when(usuarioService.obtenerCatalogosAdmin()).thenReturn(
                UsuarioCatalogosDTO.builder()
                        .roles(List.of(RolDTO.builder().idRol(1L).nombre("ADMIN").build()))
                        .nodos(List.of(NodoSimpleDTO.builder().idNodo(1L).nombre("Kennedy Central").build()))
                        .build()
        );

        mockMvc.perform(get("/api/admin/usuarios/catalogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0].nombre").value("ADMIN"))
                .andExpect(jsonPath("$.nodos[0].nombre").value("Kennedy Central"));
    }

    @Test
    void descargarPlantilla_retornaExcel200() throws Exception {
        when(cargaMasivaUsuarioService.generarPlantillaExcel()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/admin/usuarios/plantilla-estudiantes"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("plantilla_carga_estudiantes.xlsx")));
    }

    @Test
    void cargarMasiva_cuandoArchivoValido_retornaResumen200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("archivo", "estudiantes.xlsx", "application/octet-stream", new byte[]{1, 2});
        when(cargaMasivaUsuarioService.cargarEstudiantesDesdeExcel(isNull(), eq(1L), any(), eq(true)))
                .thenReturn(CargaMasivaUsuariosResponseDTO.builder()
                        .totalFilasProcesadas(2)
                        .exitosos(1)
                        .fallidos(1)
                        .vinculadosAGrupo(1)
                        .errores(List.of(CargaMasivaErrorDTO.builder().fila(3).motivo("Duplicado").build()))
                        .build());

        mockMvc.perform(multipart("/api/admin/usuarios/carga-masiva").file(file).param("idNodo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitosos").value(1))
                .andExpect(jsonPath("$.fallidos").value(1));
    }

    @Test
    void exportar_retornaExcel200() throws Exception {
        org.mockito.Mockito.doAnswer(inv -> {
            java.io.OutputStream os = inv.getArgument(4);
            os.write(new byte[]{1, 2, 3});
            return null;
        }).when(exportacionUsuarioService).exportarParaAdmin(any(), any(), any(), any(), any());

        mockMvc.perform(get("/api/admin/usuarios/exportar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("usuarios_rumbo_a_la_u.xlsx")));
    }
}
