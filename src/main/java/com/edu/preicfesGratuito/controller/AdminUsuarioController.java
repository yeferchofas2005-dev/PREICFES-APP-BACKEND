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
 * Controlador REST para la gestion integral de usuarios por parte del Administrador.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PaginaResponseDTO<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long idRol,
            @RequestParam(required = false) Long idNodo,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(usuarioService.listarParaAdmin(busqueda, idRol, idNodo, activo, page, size));
    }

    @GetMapping("/catalogos")
    public ResponseEntity<UsuarioCatalogosDTO> obtenerCatalogos() {
        return ResponseEntity.ok(usuarioService.obtenerCatalogosAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerParaAdmin(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioCrearDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearParaAdmin(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActualizarDTO request
    ) {
        return ResponseEntity.ok(usuarioService.actualizarParaAdmin(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(usuarioService.cambiarEstadoParaAdmin(userDetails.getIdUsuario(), id, request));
    }
}
