package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.ParametroGeneralActualizarDTO;
import com.edu.preicfesGratuito.dto.ParametroGeneralResponseDTO;
import com.edu.preicfesGratuito.enums.TipoParametroGeneral;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.GlobalExceptionHandler;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.service.ParametroGeneralService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminParametroGeneralControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ParametroGeneralService parametroGeneralService;

    @InjectMocks
    private AdminParametroGeneralController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listar_retorna200ConParametros() throws Exception {
        when(parametroGeneralService.listarTodos()).thenReturn(List.of(
                ParametroGeneralResponseDTO.builder()
                        .idParametro(1L)
                        .clave("MAX_NODOS_CONCURRENTES_DIA")
                        .valor("2")
                        .tipo(TipoParametroGeneral.INTEGER)
                        .build()
        ));

        mockMvc.perform(get("/api/admin/parametros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clave").value("MAX_NODOS_CONCURRENTES_DIA"))
                .andExpect(jsonPath("$[0].valor").value("2"));
    }

    @Test
    void obtenerPorClave_retorna200Y404CuandoNoExiste() throws Exception {
        when(parametroGeneralService.obtenerPorClave("DURACION_SIMULACRO_MINUTOS"))
                .thenReturn(ParametroGeneralResponseDTO.builder()
                        .clave("DURACION_SIMULACRO_MINUTOS")
                        .valor("240")
                        .tipo(TipoParametroGeneral.INTEGER)
                        .build());
        when(parametroGeneralService.obtenerPorClave("NO_EXISTE"))
                .thenThrow(new ResourceNotFoundException("Parámetro general no encontrado: NO_EXISTE"));

        mockMvc.perform(get("/api/admin/parametros/DURACION_SIMULACRO_MINUTOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("240"));

        mockMvc.perform(get("/api/admin/parametros/NO_EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_retorna200ConPayloadValidoY400CuandoInvalido() throws Exception {
        ParametroGeneralActualizarDTO valido = ParametroGeneralActualizarDTO.builder()
                .valor("300")
                .descripcion("5 horas")
                .build();
        when(parametroGeneralService.actualizar(eq("DURACION_SIMULACRO_MINUTOS"), any(ParametroGeneralActualizarDTO.class)))
                .thenReturn(ParametroGeneralResponseDTO.builder()
                        .clave("DURACION_SIMULACRO_MINUTOS")
                        .valor("300")
                        .descripcion("5 horas")
                        .tipo(TipoParametroGeneral.INTEGER)
                        .build());

        mockMvc.perform(put("/api/admin/parametros/DURACION_SIMULACRO_MINUTOS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("300"));

        ParametroGeneralActualizarDTO vacio = ParametroGeneralActualizarDTO.builder().valor("   ").build();
        mockMvc.perform(put("/api/admin/parametros/DURACION_SIMULACRO_MINUTOS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vacio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.valor").exists());

        when(parametroGeneralService.actualizar(eq("TAMANO_LOTE_RESPUESTAS"), any(ParametroGeneralActualizarDTO.class)))
                .thenThrow(new BadRequestException("Debe ser mayor o igual a 1"));
        mockMvc.perform(put("/api/admin/parametros/TAMANO_LOTE_RESPUESTAS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ParametroGeneralActualizarDTO.builder().valor("0").build()
                        )))
                .andExpect(status().isBadRequest());
    }
}
