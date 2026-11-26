package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.ParametroGeneralActualizarDTO;
import com.edu.preicfesGratuito.dto.ParametroGeneralResponseDTO;

import java.util.List;

/**
 * Contrato de negocio para consultar (con cache en memoria) y actualizar
 * los parametros generales del sistema.
 */
public interface ParametroGeneralService {

    List<ParametroGeneralResponseDTO> listarTodos();

    ParametroGeneralResponseDTO obtenerPorClave(String clave);

    int obtenerValorEntero(String clave, int valorPorDefecto);

    boolean obtenerValorBooleano(String clave, boolean valorPorDefecto);

    ParametroGeneralResponseDTO actualizar(String clave, ParametroGeneralActualizarDTO request);
}
