package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.EnvioCredencial;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EnvioCredencialRepositoryTest {

    @Autowired
    private EnvioCredencialRepository envioCredencialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void consultasAgregadasOrdenadasYReencolado_funcionanContraMySql() {
        Usuario estudiante = usuarioRepository.findByCorreo("estudiante@preicfes.com").orElseThrow();
        Usuario profesor = usuarioRepository.findByCorreo("profesor@preicfes.com").orElseThrow();
        LocalDate hoy = LocalDate.now();

        envioCredencialRepository.save(EnvioCredencial.builder()
                .usuario(estudiante)
                .estado(EstadoEnvioCredencial.PENDIENTE)
                .prioridad(2)
                .passwordCifradaTemporal("aes-1")
                .fechaProgramada(hoy)
                .build());

        envioCredencialRepository.save(EnvioCredencial.builder()
                .usuario(profesor)
                .estado(EstadoEnvioCredencial.FALLIDO)
                .prioridad(1)
                .passwordCifradaTemporal("aes-2")
                .ultimoError("Timeout SMTP")
                .fechaProgramada(hoy)
                .build());

        envioCredencialRepository.save(EnvioCredencial.builder()
                .usuario(estudiante)
                .estado(EstadoEnvioCredencial.ENVIADO)
                .prioridad(1)
                .fechaProgramada(hoy)
                .fechaEnvio(LocalDateTime.now())
                .build());

        EnvioCredencialResumenProjection resumenNodo1 = envioCredencialRepository.obtenerResumenAgregado(1L);
        assertThat(resumenNodo1.getTotalRegistros()).isGreaterThanOrEqualTo(3);
        assertThat(resumenNodo1.getPendientes()).isGreaterThanOrEqualTo(1);
        assertThat(resumenNodo1.getFallidos()).isGreaterThanOrEqualTo(1);
        assertThat(resumenNodo1.getEnviados()).isGreaterThanOrEqualTo(1);

        long enviadosHoy = envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(
                EstadoEnvioCredencial.ENVIADO, hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay()
        );
        assertThat(enviadosHoy).isGreaterThanOrEqualTo(1);

        List<EnvioCredencial> pendientes = envioCredencialRepository.buscarPendientesOrdenados(1L, PageRequest.of(0, 10));
        assertThat(pendientes).isNotEmpty();

        Page<EnvioCredencial> pagina = envioCredencialRepository.buscarRegistrosPaginados(
                1L, EstadoEnvioCredencial.FALLIDO, PageRequest.of(0, 10)
        );
        assertThat(pagina.getContent()).isNotEmpty();

        int reencolados = envioCredencialRepository.reencolarFallidos(1L, hoy);
        assertThat(reencolados).isGreaterThanOrEqualTo(1);
    }
}
