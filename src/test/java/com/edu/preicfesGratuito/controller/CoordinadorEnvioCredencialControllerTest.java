package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.EnvioCredencialItemDTO;
import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.dto.PaginaResponseDTO;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.exception.GlobalExceptionHandler;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.EnvioCredencialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CoordinadorEnvioCredencialControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnvioCredencialService envioCredencialService;

    @InjectMocks
    private CoordinadorEnvioCredencialController controller;

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

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endpointsCoordinador_deleganConIdUsuarioAutenticado() throws Exception {
        when(envioCredencialService.obtenerResumenParaCoordinador(2L)).thenReturn(
                EnvioCredencialResumenDTO.builder().totalRegistros(10).enviados(4).porcentajeAvance(40.0).build()
        );
        when(envioCredencialService.listarRegistrosParaCoordinador(2L, null, 0, 10)).thenReturn(
                PaginaResponseDTO.<EnvioCredencialItemDTO>builder()
                        .contenido(List.of())
                        .paginaActual(0)
                        .tamanoPagina(10)
                        .totalElementos(0)
                        .totalPaginas(0)
                        .ultima(true)
                        .build()
        );
        when(envioCredencialService.iniciarLoteParaCoordinador(2L)).thenReturn(
                LoteEnvioResultadoDTO.builder().iniciado(true).build()
        );
        when(envioCredencialService.reintentarFallidosParaCoordinador(2L)).thenReturn(
                LoteEnvioResultadoDTO.builder().iniciado(true).registrosAfectados(2).build()
        );

        mockMvc.perform(get("/api/directivo/envios-credenciales/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajeAvance").value(40.0));

        mockMvc.perform(get("/api/directivo/envios-credenciales/registros"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/directivo/envios-credenciales/procesar-lote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iniciado").value(true));

        mockMvc.perform(post("/api/directivo/envios-credenciales/reintentar-fallidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrosAfectados").value(2));
    }
}
