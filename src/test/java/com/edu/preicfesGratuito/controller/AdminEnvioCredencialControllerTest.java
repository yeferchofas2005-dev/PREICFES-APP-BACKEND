package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.EnvioCredencialItemDTO;
import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.dto.PaginaResponseDTO;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import com.edu.preicfesGratuito.exception.GlobalExceptionHandler;
import com.edu.preicfesGratuito.service.EnvioCredencialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminEnvioCredencialControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EnvioCredencialService envioCredencialService;

    @InjectMocks
    private AdminEnvioCredencialController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerResumen_retorna200() throws Exception {
        when(envioCredencialService.obtenerResumenParaAdmin()).thenReturn(
                EnvioCredencialResumenDTO.builder()
                        .totalRegistros(100)
                        .enviados(50)
                        .pendientes(45)
                        .fallidos(5)
                        .porcentajeAvance(50.0)
                        .limiteDiario(450)
                        .cupoRestanteHoy(400)
                        .build()
        );

        mockMvc.perform(get("/api/admin/envios-credenciales/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajeAvance").value(50.0))
                .andExpect(jsonPath("$.cupoRestanteHoy").value(400));
    }

    @Test
    void listarRegistros_retornaPagina200() throws Exception {
        when(envioCredencialService.listarRegistrosParaAdmin(EstadoEnvioCredencial.FALLIDO, 0, 10))
                .thenReturn(PaginaResponseDTO.<EnvioCredencialItemDTO>builder()
                        .contenido(List.of(EnvioCredencialItemDTO.builder()
                                .idEnvio(1L)
                                .correo("test@preicfes.com")
                                .estado(EstadoEnvioCredencial.FALLIDO)
                                .build()))
                        .paginaActual(0)
                        .tamanoPagina(10)
                        .totalElementos(1)
                        .totalPaginas(1)
                        .ultima(true)
                        .build());

        mockMvc.perform(get("/api/admin/envios-credenciales/registros?estado=FALLIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].correo").value("test@preicfes.com"));
    }

    @Test
    void procesarLoteYReintentar_retornan200() throws Exception {
        when(envioCredencialService.iniciarLoteParaAdmin()).thenReturn(
                LoteEnvioResultadoDTO.builder().iniciado(true).mensaje("Lote iniciado").build()
        );
        when(envioCredencialService.reintentarFallidosParaAdmin()).thenReturn(
                LoteEnvioResultadoDTO.builder().iniciado(true).registrosAfectados(3).build()
        );

        mockMvc.perform(post("/api/admin/envios-credenciales/procesar-lote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iniciado").value(true));

        mockMvc.perform(post("/api/admin/envios-credenciales/reintentar-fallidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrosAfectados").value(3));
    }
}
