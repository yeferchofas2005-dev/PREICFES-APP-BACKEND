package com.edu.preicfesGratuito.service;

import java.io.OutputStream;

/**
 * Servicio para exportacion eficiente en streaming (.xlsx) del listado de usuarios.
 */
public interface ExportacionUsuarioService {

    void exportarParaAdmin(
            String busqueda, Long idRol, Long idNodo, Boolean activo, OutputStream outputStream
    );

    void exportarParaCoordinador(
            Long idCoordinador, String busqueda, Long idRol, Boolean activo, OutputStream outputStream
    );
}
