package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.*;

/**
 * Servicio de gestion de usuarios para Administradores y Coordinadores de nodo.
 */
public interface UsuarioService {

    PaginaResponseDTO<UsuarioResponseDTO> listarParaAdmin(
            String busqueda, Long idRol, Long idNodo, Boolean activo, int page, int size
    );

    UsuarioResponseDTO obtenerParaAdmin(Long idUsuario);

    UsuarioResponseDTO crearParaAdmin(UsuarioCrearDTO request);

    UsuarioResponseDTO actualizarParaAdmin(Long idUsuario, UsuarioActualizarDTO request);

    UsuarioResponseDTO cambiarEstadoParaAdmin(Long idActor, Long idUsuario, CambiarEstadoDTO request);

    UsuarioCatalogosDTO obtenerCatalogosAdmin();

    PaginaResponseDTO<UsuarioResponseDTO> listarParaCoordinador(
            Long idCoordinador, String busqueda, Long idRol, Boolean activo, int page, int size
    );

    UsuarioResponseDTO obtenerParaCoordinador(Long idCoordinador, Long idUsuario);

    UsuarioResponseDTO crearParaCoordinador(Long idCoordinador, UsuarioCrearDTO request);

    UsuarioResponseDTO actualizarParaCoordinador(Long idCoordinador, Long idUsuario, UsuarioActualizarDTO request);

    UsuarioResponseDTO cambiarEstadoParaCoordinador(Long idCoordinador, Long idUsuario, CambiarEstadoDTO request);

    UsuarioCatalogosDTO obtenerCatalogosCoordinador(Long idCoordinador);
}
