package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.EnvioCredencialItemDTO;
import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.dto.PaginaResponseDTO;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;

import java.util.List;

/**
 * Servicio para gestionar la cola de envio de credenciales por correo con cuota diaria.
 */
public interface EnvioCredencialService {

    record RegistroEnvioMasivo(Usuario usuario, String passwordPlano) {}

    void registrarEnvioManual(Usuario usuario, String passwordPlano);

    void encolarLoteMasivo(List<RegistroEnvioMasivo> registros);

    EnvioCredencialResumenDTO obtenerResumenParaAdmin();

    EnvioCredencialResumenDTO obtenerResumenParaCoordinador(Long idCoordinador);

    PaginaResponseDTO<EnvioCredencialItemDTO> listarRegistrosParaAdmin(
            EstadoEnvioCredencial estado, int page, int size
    );

    PaginaResponseDTO<EnvioCredencialItemDTO> listarRegistrosParaCoordinador(
            Long idCoordinador, EstadoEnvioCredencial estado, int page, int size
    );

    LoteEnvioResultadoDTO iniciarLoteParaAdmin();

    LoteEnvioResultadoDTO iniciarLoteParaCoordinador(Long idCoordinador);

    LoteEnvioResultadoDTO reintentarFallidosParaAdmin();

    LoteEnvioResultadoDTO reintentarFallidosParaCoordinador(Long idCoordinador);

    int procesarLotePendienteSincrono(Long idNodo);
}
