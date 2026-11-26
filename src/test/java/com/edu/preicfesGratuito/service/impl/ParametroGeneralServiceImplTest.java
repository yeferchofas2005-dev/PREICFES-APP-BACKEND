package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.ParametroGeneralActualizarDTO;
import com.edu.preicfesGratuito.dto.ParametroGeneralResponseDTO;
import com.edu.preicfesGratuito.entity.ParametroGeneral;
import com.edu.preicfesGratuito.enums.TipoParametroGeneral;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.ParametroGeneralRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParametroGeneralServiceImplTest {

    @Mock
    private ParametroGeneralRepository parametroGeneralRepository;

    @InjectMocks
    private ParametroGeneralServiceImpl parametroGeneralService;

    @Test
    void listarTodos_retornaListaMapeada() {
        ParametroGeneral p1 = ParametroGeneral.builder()
                .idParametro(1L)
                .clave("DURACION_SIMULACRO_MINUTOS")
                .valor("240")
                .descripcion("Duración en minutos")
                .tipo(TipoParametroGeneral.INTEGER)
                .fechaActualizacion(LocalDateTime.now())
                .build();
        ParametroGeneral p2 = ParametroGeneral.builder()
                .idParametro(2L)
                .clave("MAX_NODOS_CONCURRENTES_DIA")
                .valor("2")
                .tipo(TipoParametroGeneral.INTEGER)
                .build();

        when(parametroGeneralRepository.findAllByOrderByClaveAsc()).thenReturn(List.of(p1, p2));

        List<ParametroGeneralResponseDTO> resultado = parametroGeneralService.listarTodos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getClave()).isEqualTo("DURACION_SIMULACRO_MINUTOS");
        assertThat(resultado.get(1).getValor()).isEqualTo("2");
    }

    @Test
    void obtenerPorClave_normalizaClaveYRetornaDto() {
        ParametroGeneral p = ParametroGeneral.builder()
                .idParametro(1L)
                .clave("TAMANO_LOTE_RESPUESTAS")
                .valor("10")
                .tipo(TipoParametroGeneral.INTEGER)
                .build();
        when(parametroGeneralRepository.findByClave("TAMANO_LOTE_RESPUESTAS")).thenReturn(Optional.of(p));

        ParametroGeneralResponseDTO dto = parametroGeneralService.obtenerPorClave("  tamano_lote_respuestas ");

        assertThat(dto.getClave()).isEqualTo("TAMANO_LOTE_RESPUESTAS");
        assertThat(dto.getValor()).isEqualTo("10");
    }

    @Test
    void obtenerPorClave_cuandoNoExiste_lanzaResourceNotFound() {
        when(parametroGeneralRepository.findByClave("NO_EXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parametroGeneralService.obtenerPorClave("no_existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPorClave_cuandoClaveVacia_lanzaBadRequest() {
        assertThatThrownBy(() -> parametroGeneralService.obtenerPorClave("   "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void obtenerValorEntero_retornaValorParseadoODefault() {
        when(parametroGeneralRepository.findByClave("DURACION_SIMULACRO_MINUTOS"))
                .thenReturn(Optional.of(ParametroGeneral.builder()
                        .clave("DURACION_SIMULACRO_MINUTOS")
                        .valor("300")
                        .tipo(TipoParametroGeneral.INTEGER)
                        .build()));
        when(parametroGeneralRepository.findByClave("OTRO")).thenReturn(Optional.empty());

        assertThat(parametroGeneralService.obtenerValorEntero("DURACION_SIMULACRO_MINUTOS", 240)).isEqualTo(300);
        assertThat(parametroGeneralService.obtenerValorEntero("OTRO", 15)).isEqualTo(15);
    }

    @Test
    void obtenerValorBooleano_retornaValorParseadoODefault() {
        when(parametroGeneralRepository.findByClave("MOSTRAR_RETROALIMENTACION_SIMULACRO"))
                .thenReturn(Optional.of(ParametroGeneral.builder()
                        .clave("MOSTRAR_RETROALIMENTACION_SIMULACRO")
                        .valor("true")
                        .tipo(TipoParametroGeneral.BOOLEAN)
                        .build()));
        when(parametroGeneralRepository.findByClave("OTRO_BOOL")).thenReturn(Optional.empty());

        assertThat(parametroGeneralService.obtenerValorBooleano("MOSTRAR_RETROALIMENTACION_SIMULACRO", false)).isTrue();
        assertThat(parametroGeneralService.obtenerValorBooleano("OTRO_BOOL", false)).isFalse();
    }

    @Test
    void actualizar_enteroNoNegativo_aceptaCeroYRechazaNegativos() {
        ParametroGeneral jitter = ParametroGeneral.builder()
                .clave("JITTER_MAXIMO_CHECKPOINT_MS")
                .valor("2000")
                .tipo(TipoParametroGeneral.INTEGER)
                .build();
        when(parametroGeneralRepository.findByClave("JITTER_MAXIMO_CHECKPOINT_MS"))
                .thenReturn(Optional.of(jitter));
        when(parametroGeneralRepository.saveAndFlush(any(ParametroGeneral.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ParametroGeneralResponseDTO cero = parametroGeneralService.actualizar(
                "JITTER_MAXIMO_CHECKPOINT_MS",
                ParametroGeneralActualizarDTO.builder().valor("0").build()
        );
        assertThat(cero.getValor()).isEqualTo("0");

        assertThatThrownBy(() -> parametroGeneralService.actualizar(
                "JITTER_MAXIMO_CHECKPOINT_MS",
                ParametroGeneralActualizarDTO.builder().valor("-1").build()
        )).isInstanceOf(BadRequestException.class);
    }

    @Test
    void actualizar_enteroValido_actualizaYPersiste() {
        ParametroGeneral existente = ParametroGeneral.builder()
                .idParametro(1L)
                .clave("MAX_NODOS_CONCURRENTES_DIA")
                .valor("1")
                .descripcion("Inicial")
                .tipo(TipoParametroGeneral.INTEGER)
                .build();
        when(parametroGeneralRepository.findByClave("MAX_NODOS_CONCURRENTES_DIA"))
                .thenReturn(Optional.of(existente));
        when(parametroGeneralRepository.saveAndFlush(any(ParametroGeneral.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ParametroGeneralResponseDTO actualizado = parametroGeneralService.actualizar(
                "max_nodos_concurrentes_dia",
                ParametroGeneralActualizarDTO.builder()
                        .valor(" 3 ")
                        .descripcion("Capacidad ampliada")
                        .build()
        );

        assertThat(actualizado.getValor()).isEqualTo("3");
        assertThat(actualizado.getDescripcion()).isEqualTo("Capacidad ampliada");
        verify(parametroGeneralRepository).saveAndFlush(existente);
    }

    @Test
    void actualizar_conTiposInvalidos_lanzaBadRequestSinGuardar() {
        ParametroGeneral entero = ParametroGeneral.builder()
                .clave("DURACION_SIMULACRO_MINUTOS")
                .valor("240")
                .tipo(TipoParametroGeneral.INTEGER)
                .build();
        ParametroGeneral booleano = ParametroGeneral.builder()
                .clave("MODO_MANTENIMIENTO")
                .valor("false")
                .tipo(TipoParametroGeneral.BOOLEAN)
                .build();
        ParametroGeneral decimal = ParametroGeneral.builder()
                .clave("UMBRAL_DECIMAL")
                .valor("0.75")
                .tipo(TipoParametroGeneral.DECIMAL)
                .build();

        when(parametroGeneralRepository.findByClave("DURACION_SIMULACRO_MINUTOS"))
                .thenReturn(Optional.of(entero));
        when(parametroGeneralRepository.findByClave("MODO_MANTENIMIENTO"))
                .thenReturn(Optional.of(booleano));
        when(parametroGeneralRepository.findByClave("UMBRAL_DECIMAL"))
                .thenReturn(Optional.of(decimal));

        assertThatThrownBy(() -> parametroGeneralService.actualizar(
                "DURACION_SIMULACRO_MINUTOS",
                ParametroGeneralActualizarDTO.builder().valor("abc").build()
        )).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> parametroGeneralService.actualizar(
                "DURACION_SIMULACRO_MINUTOS",
                ParametroGeneralActualizarDTO.builder().valor("0").build()
        )).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> parametroGeneralService.actualizar(
                "MODO_MANTENIMIENTO",
                ParametroGeneralActualizarDTO.builder().valor("si").build()
        )).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> parametroGeneralService.actualizar(
                "UMBRAL_DECIMAL",
                ParametroGeneralActualizarDTO.builder().valor("no-num").build()
        )).isInstanceOf(BadRequestException.class);

        verify(parametroGeneralRepository, never()).saveAndFlush(any());
    }

    @Test
    void actualizar_booleanoYDecimalValidos_normalizaCorrectamente() {
        ParametroGeneral booleano = ParametroGeneral.builder()
                .clave("MODO_MANTENIMIENTO")
                .valor("false")
                .tipo(TipoParametroGeneral.BOOLEAN)
                .build();
        when(parametroGeneralRepository.findByClave("MODO_MANTENIMIENTO"))
                .thenReturn(Optional.of(booleano));
        when(parametroGeneralRepository.saveAndFlush(any(ParametroGeneral.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ParametroGeneralResponseDTO dto = parametroGeneralService.actualizar(
                "MODO_MANTENIMIENTO",
                ParametroGeneralActualizarDTO.builder().valor("TRUE").build()
        );

        assertThat(dto.getValor()).isEqualTo("true");
    }
}
