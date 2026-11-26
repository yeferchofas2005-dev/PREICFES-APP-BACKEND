package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.ParametroGeneral;
import com.edu.preicfesGratuito.enums.TipoParametroGeneral;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ParametroGeneralRepositoryTest {

    @Autowired
    private ParametroGeneralRepository parametroGeneralRepository;

    @Test
    void findAllByOrderByClaveAsc_retornaParametrosInicialesOrdenados() {
        List<ParametroGeneral> parametros = parametroGeneralRepository.findAllByOrderByClaveAsc();

        assertThat(parametros)
                .extracting(ParametroGeneral::getClave)
                .containsSubsequence(
                        "CACHE_SIMULACROS_TTL_MINUTOS",
                        "CORREO_DELAY_ENTRE_ENVIOS_MS",
                        "DURACION_SIMULACRO_MINUTOS",
                        "JITTER_MAXIMO_CHECKPOINT_MS",
                        "MAX_CORREOS_POR_DIA",
                        "MAX_NODOS_CONCURRENTES_DIA",
                        "MOSTRAR_RETROALIMENTACION_SIMULACRO",
                        "PERMITIR_CODOCENCIA_SALON",
                        "TAMANO_LOTE_RESPUESTAS",
                        "TIEMPO_GRACIA_CIERRE_SEGUNDOS",
                        "VENTANA_ACTIVACION_PROFESOR_MINUTOS"
                );
    }

    @Test
    void findByClave_cuandoExisteYNoExiste_retornaResultadoEsperado() {
        Optional<ParametroGeneral> encontrado = parametroGeneralRepository.findByClave("MAX_NODOS_CONCURRENTES_DIA");
        Optional<ParametroGeneral> inexistente = parametroGeneralRepository.findByClave("CLAVE_INEXISTENTE_TEST");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTipo()).isEqualTo(TipoParametroGeneral.INTEGER);
        assertThat(inexistente).isEmpty();
    }

    @Test
    void saveAndFlush_actualizaValorYFechaActualizacion() {
        ParametroGeneral parametro = parametroGeneralRepository.findByClave("TAMANO_LOTE_RESPUESTAS").orElseThrow();
        parametro.setValor("25");
        parametro.setDescripcion("Actualizado en prueba de repositorio");

        ParametroGeneral guardado = parametroGeneralRepository.saveAndFlush(parametro);

        assertThat(guardado.getValor()).isEqualTo("25");
        assertThat(guardado.getDescripcion()).isEqualTo("Actualizado en prueba de repositorio");
        assertThat(guardado.getFechaActualizacion()).isNotNull();
    }
}
