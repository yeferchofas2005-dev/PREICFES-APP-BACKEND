package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.EnvioCredencialItemDTO;
import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.dto.PaginaResponseDTO;
import com.edu.preicfesGratuito.entity.EnvioCredencial;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.EnvioCredencialRepository;
import com.edu.preicfesGratuito.repository.EnvioCredencialResumenProjection;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.service.EnvioCredencialService;
import com.edu.preicfesGratuito.util.AesCryptoUtil;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementacion de cola elastica para envio de credenciales respetando el limite diario de Gmail.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioCredencialServiceImpl implements EnvioCredencialService {

    private static final int BATCH_INSERT_SIZE = 500;
    private static final int MAX_ERROR_LENGTH = 480;

    private final EnvioCredencialRepository envioCredencialRepository;
    private final UsuarioRepository usuarioRepository;
    private final AesCryptoUtil aesCryptoUtil;
    private final JavaMailSender mailSender;

    private final AtomicBoolean loteEnProceso = new AtomicBoolean(false);

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.max-per-day:450}")
    private int maxCorreosPorDia;

    @Value("${app.mail.platform-url:http://localhost:4200}")
    private String platformUrl;

    @Value("${app.mail.sender-name:RumboALaU - PreICFES Gratuito}")
    private String senderName;

    @Value("${app.mail.delay-between-emails-ms:1000}")
    private long delayBetweenEmailsMs;

    @Override
    @Transactional
    public void registrarEnvioManual(Usuario usuario, String passwordPlano) {
        LocalDate hoy = LocalDate.now();
        EnvioCredencial registro = EnvioCredencial.builder()
                .usuario(usuario)
                .estado(EstadoEnvioCredencial.PENDIENTE)
                .prioridad(1)
                .passwordCifradaTemporal(aesCryptoUtil.encrypt(passwordPlano))
                .intentos(0)
                .fechaProgramada(hoy)
                .build();

        long enviadosHoy = contarEnviadosHoy();
        if (enviadosHoy >= maxCorreosPorDia) {
            registro.setFechaProgramada(hoy.plusDays(1));
            envioCredencialRepository.save(registro);
            return;
        }

        registro = envioCredencialRepository.save(registro);
        intentarEnviarRegistroIndividual(registro, passwordPlano);
    }

    @Override
    @Transactional
    public void encolarLoteMasivo(List<RegistroEnvioMasivo> registros) {
        if (registros == null || registros.isEmpty()) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        List<EnvioCredencial> lote = new ArrayList<>(Math.min(registros.size(), BATCH_INSERT_SIZE));

        for (RegistroEnvioMasivo item : registros) {
            lote.add(EnvioCredencial.builder()
                    .usuario(item.usuario())
                    .estado(EstadoEnvioCredencial.PENDIENTE)
                    .prioridad(2)
                    .passwordCifradaTemporal(aesCryptoUtil.encrypt(item.passwordPlano()))
                    .intentos(0)
                    .fechaProgramada(hoy)
                    .build());

            if (lote.size() >= BATCH_INSERT_SIZE) {
                envioCredencialRepository.saveAll(lote);
                lote.clear();
            }
        }
        if (!lote.isEmpty()) {
            envioCredencialRepository.saveAll(lote);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioCredencialResumenDTO obtenerResumenParaAdmin() {
        return construirResumen(null);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioCredencialResumenDTO obtenerResumenParaCoordinador(Long idCoordinador) {
        return construirResumen(obtenerIdNodoCoordinador(idCoordinador));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<EnvioCredencialItemDTO> listarRegistrosParaAdmin(
            EstadoEnvioCredencial estado, int page, int size
    ) {
        return listarRegistrosInterno(null, estado, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<EnvioCredencialItemDTO> listarRegistrosParaCoordinador(
            Long idCoordinador, EstadoEnvioCredencial estado, int page, int size
    ) {
        return listarRegistrosInterno(obtenerIdNodoCoordinador(idCoordinador), estado, page, size);
    }

    @Override
    public LoteEnvioResultadoDTO iniciarLoteParaAdmin() {
        return lanzarLoteAsincrono(null);
    }

    @Override
    public LoteEnvioResultadoDTO iniciarLoteParaCoordinador(Long idCoordinador) {
        Long idNodo = obtenerIdNodoCoordinador(idCoordinador);
        return lanzarLoteAsincrono(idNodo);
    }

    @Override
    @Transactional
    public LoteEnvioResultadoDTO reintentarFallidosParaAdmin() {
        int reencolados = envioCredencialRepository.reencolarFallidos(null, LocalDate.now());
        return LoteEnvioResultadoDTO.builder()
                .iniciado(reencolados > 0)
                .mensaje(reencolados > 0
                        ? "Se reencolaron " + reencolados + " registros fallidos a estado PENDIENTE."
                        : "No hay registros fallidos disponibles para reintentar.")
                .cupoRestanteHoy(Math.max(0, maxCorreosPorDia - contarEnviadosHoy()))
                .pendientesEnCola(construirResumen(null).getPendientes())
                .registrosAfectados(reencolados)
                .build();
    }

    @Override
    @Transactional
    public LoteEnvioResultadoDTO reintentarFallidosParaCoordinador(Long idCoordinador) {
        Long idNodo = obtenerIdNodoCoordinador(idCoordinador);
        int reencolados = envioCredencialRepository.reencolarFallidos(idNodo, LocalDate.now());
        return LoteEnvioResultadoDTO.builder()
                .iniciado(reencolados > 0)
                .mensaje(reencolados > 0
                        ? "Se reencolaron " + reencolados + " registros fallidos de tu nodo."
                        : "No hay registros fallidos en tu nodo para reintentar.")
                .cupoRestanteHoy(Math.max(0, maxCorreosPorDia - contarEnviadosHoy()))
                .pendientesEnCola(construirResumen(idNodo).getPendientes())
                .registrosAfectados(reencolados)
                .build();
    }

    @Scheduled(cron = "${app.mail.daily-cron:0 0 8 * * *}")
    public void ejecutarEnvioDiarioAutomatico() {
        if (!credencialesSmtpConfiguradas()) {
            return;
        }
        if (construirResumen(null).getPendientes() > 0) {
            lanzarLoteAsincrono(null);
        }
    }

    private LoteEnvioResultadoDTO lanzarLoteAsincrono(Long idNodo) {
        if (!credencialesSmtpConfiguradas()) {
            throw new BadRequestException(
                    "Las credenciales SMTP (MAIL_USERNAME y MAIL_PASSWORD) no están configuradas en el archivo .env"
            );
        }

        EnvioCredencialResumenDTO resumen = construirResumen(idNodo);
        if (resumen.getPendientes() == 0) {
            return LoteEnvioResultadoDTO.builder()
                    .iniciado(false)
                    .mensaje("No hay envíos pendientes por procesar.")
                    .cupoRestanteHoy(resumen.getCupoRestanteHoy())
                    .pendientesEnCola(0)
                    .registrosAfectados(0)
                    .build();
        }
        if (resumen.getCupoRestanteHoy() <= 0) {
            return LoteEnvioResultadoDTO.builder()
                    .iniciado(false)
                    .mensaje("Se alcanzó el cupo diario global (" + maxCorreosPorDia + "). Los pendientes saldrán mañana.")
                    .cupoRestanteHoy(0)
                    .pendientesEnCola(resumen.getPendientes())
                    .registrosAfectados(0)
                    .build();
        }
        if (!loteEnProceso.compareAndSet(false, true)) {
            return LoteEnvioResultadoDTO.builder()
                    .iniciado(false)
                    .mensaje("Ya hay un lote de envíos en ejecución.")
                    .cupoRestanteHoy(resumen.getCupoRestanteHoy())
                    .pendientesEnCola(resumen.getPendientes())
                    .registrosAfectados(0)
                    .build();
        }

        CompletableFuture.runAsync(() -> procesarLoteConLockTomado(idNodo));

        return LoteEnvioResultadoDTO.builder()
                .iniciado(true)
                .mensaje("Lote iniciado en segundo plano. Cupo disponible hoy: " + resumen.getCupoRestanteHoy())
                .cupoRestanteHoy(resumen.getCupoRestanteHoy())
                .pendientesEnCola(resumen.getPendientes())
                .registrosAfectados((int) Math.min(resumen.getCupoRestanteHoy(), resumen.getPendientes()))
                .build();
    }

    @Override
    public int procesarLotePendienteSincrono(Long idNodo) {
        if (!loteEnProceso.compareAndSet(false, true)) {
            return 0;
        }
        return procesarLoteConLockTomado(idNodo);
    }

    private int procesarLoteConLockTomado(Long idNodo) {
        int enviadosExitosos = 0;
        try {
            long cupoRestante = Math.max(0, maxCorreosPorDia - contarEnviadosHoy());
            if (cupoRestante == 0) {
                return 0;
            }

            List<EnvioCredencial> pendientes = envioCredencialRepository.buscarPendientesOrdenados(
                    idNodo, PageRequest.of(0, (int) Math.min(cupoRestante, Integer.MAX_VALUE))
            );

            for (int i = 0; i < pendientes.size(); i++) {
                EnvioCredencial registro = pendientes.get(i);
                String passwordPlano;
                try {
                    passwordPlano = aesCryptoUtil.decrypt(registro.getPasswordCifradaTemporal());
                } catch (Exception ex) {
                    marcarFallido(registro, "No se pudo descifrar la contraseña temporal en cola");
                    continue;
                }

                ResultadoEnvioIndividual res = intentarEnviarRegistroIndividual(registro, passwordPlano);
                if (res == ResultadoEnvioIndividual.ENVIADO) {
                    enviadosExitosos++;
                } else if (res == ResultadoEnvioIndividual.CUOTA_EXCEDIDA) {
                    break;
                }

                if (delayBetweenEmailsMs > 0 && i < pendientes.size() - 1) {
                    try {
                        Thread.sleep(delayBetweenEmailsMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            loteEnProceso.set(false);
        }
        return enviadosExitosos;
    }

    private ResultadoEnvioIndividual intentarEnviarRegistroIndividual(EnvioCredencial registro, String passwordPlano) {
        if (!credencialesSmtpConfiguradas()) {
            registro.setEstado(EstadoEnvioCredencial.PENDIENTE);
            registro.setUltimoError("Pendiente: credenciales SMTP no configuradas en .env");
            envioCredencialRepository.save(registro);
            return ResultadoEnvioIndividual.PENDIENTE;
        }

        try {
            enviarCorreoCredenciales(registro.getUsuario(), passwordPlano);
            registro.setEstado(EstadoEnvioCredencial.ENVIADO);
            registro.setFechaEnvio(LocalDateTime.now());
            registro.setPasswordCifradaTemporal(null);
            registro.setUltimoError(null);
            registro.setIntentos(registro.getIntentos() + 1);
            envioCredencialRepository.save(registro);
            return ResultadoEnvioIndividual.ENVIADO;
        } catch (Exception ex) {
            log.warn("Error enviando credenciales a {}: {}", registro.getUsuario().getCorreo(), ex.getMessage());
            if (esErrorCuotaGmail(ex)) {
                registro.setEstado(EstadoEnvioCredencial.PENDIENTE);
                registro.setFechaProgramada(LocalDate.now().plusDays(1));
                registro.setUltimoError("Cuota diaria alcanzada en Gmail; reprogramado para mañana");
                envioCredencialRepository.save(registro);
                return ResultadoEnvioIndividual.CUOTA_EXCEDIDA;
            }
            marcarFallido(registro, ex.getMessage());
            return ResultadoEnvioIndividual.FALLIDO;
        }
    }

    private void marcarFallido(EnvioCredencial registro, String mensaje) {
        registro.setEstado(EstadoEnvioCredencial.FALLIDO);
        registro.setIntentos(registro.getIntentos() + 1);
        registro.setUltimoError(truncar(mensaje));
        envioCredencialRepository.save(registro);
    }

    private void enviarCorreoCredenciales(Usuario usuario, String passwordPlano) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

        helper.setFrom(new InternetAddress(mailUsername, senderName, StandardCharsets.UTF_8.name()));
        helper.setTo(usuario.getCorreo());
        helper.setSubject("Tus credenciales de acceso — RumboALaU (PreICFES Gratuito)");

        String nodoNombre = usuario.getNodo() != null ? usuario.getNodo().getNombre() : "General";
        String html = """
                <div style="font-family: Arial, sans-serif; color: #1f2a63; max-width: 600px; margin: 0 auto;">
                  <div style="background: #1f2a63; color: #ffffff; padding: 20px; border-radius: 10px 10px 0 0;">
                    <h2 style="margin: 0;">Bienvenido(a) a RumboALaU</h2>
                    <p style="margin: 4px 0 0; opacity: 0.9;">PreICFES Gratuito Jóvenes Bogotá</p>
                  </div>
                  <div style="background: #ffffff; padding: 24px; border: 1px solid #e5e7eb; border-radius: 0 0 10px 10px;">
                    <p>Hola <strong>%s %s</strong>,</p>
                    <p>Tu cuenta ha sido creada en la plataforma de simulacros tipo ICFES. Estos son tus datos de acceso:</p>
                    <div style="background: #f5f7fa; padding: 14px 18px; border-radius: 8px; border-left: 4px solid #2f5fd0; margin: 16px 0;">
                      <p style="margin: 4px 0;"><strong>Correo:</strong> %s</p>
                      <p style="margin: 4px 0;"><strong>Contraseña inicial:</strong> <code>%s</code></p>
                      <p style="margin: 4px 0;"><strong>Rol:</strong> %s</p>
                      <p style="margin: 4px 0;"><strong>Nodo:</strong> %s</p>
                    </div>
                    <p>Ingresa en: <a href="%s" style="color: #2f5fd0; font-weight: bold;">%s</a></p>
                    <p style="font-size: 13px; color: #4b5563;">Te recomendamos cambiar tu contraseña o resguardar tus credenciales de forma segura.</p>
                  </div>
                </div>
                """.formatted(
                escapeHtml(usuario.getNombres()),
                escapeHtml(usuario.getApellidos()),
                escapeHtml(usuario.getCorreo()),
                escapeHtml(passwordPlano),
                escapeHtml(usuario.getRol() != null ? usuario.getRol().getNombre() : "ESTUDIANTE"),
                escapeHtml(nodoNombre),
                escapeHtml(platformUrl),
                escapeHtml(platformUrl)
        );

        helper.setText(html, true);
        mailSender.send(mimeMessage);
    }

    private EnvioCredencialResumenDTO construirResumen(Long idNodo) {
        EnvioCredencialResumenProjection stats = envioCredencialRepository.obtenerResumenAgregado(idNodo);
        long total = stats.getTotalRegistros();
        long enviados = stats.getEnviados();
        long pendientes = stats.getPendientes();
        long procesando = stats.getProcesando();
        long fallidos = stats.getFallidos();

        double porcentaje = total == 0 ? 0.0 : Math.round((enviados * 1000.0) / total) / 10.0;
        long enviadosHoy = contarEnviadosHoy();
        long cupoRestante = Math.max(0, maxCorreosPorDia - enviadosHoy);
        int diasEstimados = (pendientes == 0 || maxCorreosPorDia <= 0)
                ? 0
                : (int) Math.ceil((double) Math.max(0, pendientes - cupoRestante) / maxCorreosPorDia) + (cupoRestante > 0 ? 1 : 0);

        return EnvioCredencialResumenDTO.builder()
                .totalRegistros(total)
                .enviados(enviados)
                .pendientes(pendientes)
                .procesando(procesando)
                .fallidos(fallidos)
                .porcentajeAvance(porcentaje)
                .enviadosHoyGlobal(enviadosHoy)
                .limiteDiario(maxCorreosPorDia)
                .cupoRestanteHoy(cupoRestante)
                .loteEnProceso(loteEnProceso.get())
                .diasEstimadosRestantes(diasEstimados)
                .build();
    }

    private PaginaResponseDTO<EnvioCredencialItemDTO> listarRegistrosInterno(
            Long idNodo, EstadoEnvioCredencial estado, int page, int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "idEnvio"));

        Page<EnvioCredencial> pagina = envioCredencialRepository.buscarRegistrosPaginados(idNodo, estado, pageable);
        List<EnvioCredencialItemDTO> items = pagina.getContent().stream()
                .map(e -> EnvioCredencialItemDTO.builder()
                        .idEnvio(e.getIdEnvio())
                        .idUsuario(e.getUsuario().getIdUsuario())
                        .nombreCompleto(e.getUsuario().getNombres() + " " + e.getUsuario().getApellidos())
                        .documento(e.getUsuario().getDocumento())
                        .correo(e.getUsuario().getCorreo())
                        .rol(e.getUsuario().getRol() != null ? e.getUsuario().getRol().getNombre() : null)
                        .nodo(e.getUsuario().getNodo() != null ? e.getUsuario().getNodo().getNombre() : "Global")
                        .estado(e.getEstado())
                        .prioridad(e.getPrioridad())
                        .intentos(e.getIntentos())
                        .ultimoError(e.getUltimoError())
                        .fechaProgramada(e.getFechaProgramada())
                        .fechaEnvio(e.getFechaEnvio())
                        .build())
                .toList();

        return PaginaResponseDTO.<EnvioCredencialItemDTO>builder()
                .contenido(items)
                .paginaActual(pagina.getNumber())
                .tamanoPagina(pagina.getSize())
                .totalElementos(pagina.getTotalElements())
                .totalPaginas(pagina.getTotalPages())
                .ultima(pagina.isLast())
                .build();
    }

    private long contarEnviadosHoy() {
        LocalDate hoy = LocalDate.now();
        return envioCredencialRepository.countByEstadoAndFechaEnvioGreaterThanEqualAndFechaEnvioLessThan(
                EstadoEnvioCredencial.ENVIADO,
                hoy.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay()
        );
    }

    private Long obtenerIdNodoCoordinador(Long idCoordinador) {
        Usuario coordinador = usuarioRepository.findByIdWithRolAndNodo(idCoordinador)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinador no encontrado con id: " + idCoordinador));
        if (coordinador.getNodo() == null) {
            throw new BadRequestException("El coordinador no tiene un nodo asignado");
        }
        return coordinador.getNodo().getIdNodo();
    }

    private boolean credencialesSmtpConfiguradas() {
        return mailUsername != null && !mailUsername.isBlank()
                && !mailUsername.contains("tu_correo@")
                && mailPassword != null && !mailPassword.isBlank()
                && !mailPassword.contains("tu_contrasena");
    }

    private boolean esErrorCuotaGmail(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("daily user sending quota")
                || lower.contains("5.4.5")
                || lower.contains("too many messages")
                || lower.contains("quota exceeded");
    }

    private String truncar(String text) {
        if (text == null) return "Error desconocido al enviar correo";
        return text.length() <= MAX_ERROR_LENGTH ? text : text.substring(0, MAX_ERROR_LENGTH);
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private enum ResultadoEnvioIndividual {
        ENVIADO,
        PENDIENTE,
        FALLIDO,
        CUOTA_EXCEDIDA
    }
}
