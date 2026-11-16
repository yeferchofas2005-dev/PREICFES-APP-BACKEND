package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para que el Coordinador (DIRECTIVO) gestione profesores
 * y estudiantes vinculados a su nodo.
 */
@RestController
@RequestMapping("/api/directivo/usuarios")
@RequiredArgsConstructor
public class CoordinadorUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PaginaResponseDTO<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long idRol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                usuarioService.listarParaCoordinador(userDetails.getIdUsuario(), busqueda, idRol, activo, page, size)
        );
    }

    @GetMapping("/catalogos")
    public ResponseEntity<UsuarioCatalogosDTO> obtenerCatalogos(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(usuarioService.obtenerCatalogosCoordinador(userDetails.getIdUsuario()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(usuarioService.obtenerParaCoordinador(userDetails.getIdUsuario(), id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(
            @Valid @RequestBody UsuarioCrearDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crearParaCoordinador(userDetails.getIdUsuario(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActualizarDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(usuarioService.actualizarParaCoordinador(userDetails.getIdUsuario(), id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                usuarioService.cambiarEstadoParaCoordinador(userDetails.getIdUsuario(), id, request)
        );
    }
}
