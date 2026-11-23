package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.entity.EnvioCredencial;
import com.edu.preicfesGratuito.entity.Nodo;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import com.edu.preicfesGratuito.repository.EnvioCredencialRepository;
import com.edu.preicfesGratuito.repository.EnvioCredencialResumenProjection;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.util.AesCryptoUtil;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioCredencialServiceImplTest {

    @Mock
    private EnvioCredencialRepository envioCredencialRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JavaMailSender mailSender;

    private AesCryptoUtil aesCryptoUtil;
    private EnvioCredencialServiceImpl service;
    private Usuario estudiante;
    private Usuario coordinador;

    @BeforeEach
    void setUp() {
        aesCryptoUtil = new AesCryptoUtil("clave-test-envio-credenciales-2026");
        service = new EnvioCredencialServiceImpl(
                envioCredencialRepository, usuarioRepository, aesCryptoUtil, mailSender
        );
        ReflectionTestUtils.setField(service, "mailUsername", "remitente@gmail.com");
        ReflectionTestUtils.setField(service, "mailPassword", "abcd efgh ijkl mnop");
        ReflectionTestUtils.setField(service, "maxCorreosPorDia", 450);
        ReflectionTestUtils.setField(service, "platformUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(service, "senderName", "RumboALaU");
        ReflectionTestUtils.setField(service, "delayBetweenEmailsMs", 0L);

        Nodo nodo = Nodo.builder().idNodo(1L).nombre("Kennedy Central").build();
        estudiante = Usuario.builder()
                .idUsuario(10L)
                .nombres("Laura")
                .apellidos("Gomez")
                .documento("100999888")
                .correo("laura@preicfes.com")
                .rol(Rol.builder().idRol(4L).nombre("ESTUDIANTE").build())
                .nodo(nodo)
                .activo(true)
                .build();
        coordinador = Usuario.builder()
                .idUsuario(2L)
                .rol(Rol.builder().idRol(2L).nombre("DIRECTIVO").build())
                .nodo(nodo)
                .build();
    }

    @Test
    void registrarEnvioManual_cuandoHayCupo_enviaYBorraPasswordTemporal() {
        when(envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(any(), any(), any()))
                .thenReturn(10L);
        when(envioCredencialRepository.save(any(EnvioCredencial.class))).thenAnswer(inv -> {
            EnvioCredencial e = inv.getArgument(0);
            if (e.getIdEnvio() == null) e.setIdEnvio(1L);
            return e;
        });
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        service.registrarEnvioManual(estudiante, "MiClave123");

        ArgumentCaptor<EnvioCredencial> captor = ArgumentCaptor.forClass(EnvioCredencial.class);
        verify(envioCredencialRepository, times(2)).save(captor.capture());
        EnvioCredencial finalGuardado = captor.getValue();
        assertThat(finalGuardado.getEstado()).isEqualTo(EstadoEnvioCredencial.ENVIADO);
        assertThat(finalGuardado.getPasswordCifradaTemporal()).isNull();
        assertThat(finalGuardado.getFechaEnvio()).isNotNull();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void registrarEnvioManual_cuandoCupoAgotado_dejaPendienteParaMananaSinError() {
        when(envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(any(), any(), any()))
                .thenReturn(450L);
        when(envioCredencialRepository.save(any(EnvioCredencial.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarEnvioManual(estudiante, "MiClave123");

        ArgumentCaptor<EnvioCredencial> captor = ArgumentCaptor.forClass(EnvioCredencial.class);
        verify(envioCredencialRepository).save(captor.capture());
        EnvioCredencial guardado = captor.getValue();
        assertThat(guardado.getEstado()).isEqualTo(EstadoEnvioCredencial.PENDIENTE);
        assertThat(guardado.getFechaProgramada()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(aesCryptoUtil.decrypt(guardado.getPasswordCifradaTemporal())).isEqualTo("MiClave123");
        verifyNoInteractions(mailSender);
    }

    @Test
    void procesarLotePendienteSincrono_detieneSiGmailReportaCuotaExcedida() {
        EnvioCredencial reg1 = EnvioCredencial.builder()
                .idEnvio(1L)
                .usuario(estudiante)
                .estado(EstadoEnvioCredencial.PENDIENTE)
                .passwordCifradaTemporal(aesCryptoUtil.encrypt("Clave1"))
                .intentos(0)
                .fechaProgramada(LocalDate.now())
                .build();
        EnvioCredencial reg2 = EnvioCredencial.builder()
                .idEnvio(2L)
                .usuario(estudiante)
                .estado(EstadoEnvioCredencial.PENDIENTE)
                .passwordCifradaTemporal(aesCryptoUtil.encrypt("Clave2"))
                .intentos(0)
                .fechaProgramada(LocalDate.now())
                .build();

        when(envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(any(), any(), any()))
                .thenReturn(0L);
        when(envioCredencialRepository.buscarPendientesOrdenados(isNull(), any(Pageable.class)))
                .thenReturn(List.of(reg1, reg2));
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("550 5.4.5 Daily user sending quota exceeded"))
                .when(mailSender).send(any(MimeMessage.class));
        when(envioCredencialRepository.save(any(EnvioCredencial.class))).thenAnswer(inv -> inv.getArgument(0));

        int enviados = service.procesarLotePendienteSincrono(null);

        assertThat(enviados).isZero();
        assertThat(reg1.getEstado()).isEqualTo(EstadoEnvioCredencial.PENDIENTE);
        assertThat(reg1.getFechaProgramada()).isEqualTo(LocalDate.now().plusDays(1));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void obtenerResumenParaCoordinador_aislaPorNodoYCalculaCupoGlobal() {
        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordinador));
        when(envioCredencialRepository.obtenerResumenAgregado(1L)).thenReturn(new EnvioCredencialResumenProjection() {
            @Override public long getTotalRegistros() { return 1000; }
            @Override public long getEnviados() { return 250; }
            @Override public long getPendientes() { return 700; }
            @Override public long getProcesando() { return 0; }
            @Override public long getFallidos() { return 50; }
        });
        when(envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(any(), any(), any()))
                .thenReturn(150L);

        EnvioCredencialResumenDTO resumen = service.obtenerResumenParaCoordinador(2L);

        assertThat(resumen.getPorcentajeAvance()).isEqualTo(25.0);
        assertThat(resumen.getEnviadosHoyGlobal()).isEqualTo(150L);
        assertThat(resumen.getCupoRestanteHoy()).isEqualTo(300L);
        assertThat(resumen.getDiasEstimadosRestantes()).isEqualTo(2);
    }

    @Test
    void reintentarFallidosParaCoordinador_reencolaSoloSuNodo() {
        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordinador));
        when(envioCredencialRepository.reencolarFallidos(eq(1L), any(LocalDate.class))).thenReturn(5);
        when(envioCredencialRepository.obtenerResumenAgregado(1L)).thenReturn(new EnvioCredencialResumenProjection() {
            @Override public long getTotalRegistros() { return 20; }
            @Override public long getEnviados() { return 10; }
            @Override public long getPendientes() { return 10; }
            @Override public long getProcesando() { return 0; }
            @Override public long getFallidos() { return 0; }
        });
        when(envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(any(), any(), any()))
                .thenReturn(10L);

        LoteEnvioResultadoDTO res = service.reintentarFallidosParaCoordinador(2L);

        assertThat(res.isIniciado()).isTrue();
        assertThat(res.getRegistrosAfectados()).isEqualTo(5);
    }
}
