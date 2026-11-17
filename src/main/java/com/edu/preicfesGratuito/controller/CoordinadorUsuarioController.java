package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.CargaMasivaUsuarioService;
import com.edu.preicfesGratuito.service.ExportacionUsuarioService;
import com.edu.preicfesGratuito.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controlador REST para que el Coordinador (DIRECTIVO) gestione profesores
 * y estudiantes vinculados a su nodo.
 */
@RestController
@RequestMapping("/api/directivo/usuarios")
@RequiredArgsConstructor
public class CoordinadorUsuarioController {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final UsuarioService usuarioService;
    private final CargaMasivaUsuarioService cargaMasivaUsuarioService;
    private final ExportacionUsuarioService exportacionUsuarioService;

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

    @GetMapping("/plantilla-estudiantes")
    public ResponseEntity<byte[]> descargarPlantilla() {
        byte[] contenido = cargaMasivaUsuarioService.generarPlantillaExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla_carga_estudiantes.xlsx\"")
                .contentType(EXCEL_MEDIA_TYPE)
                .body(contenido);
    }

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargaMasivaUsuariosResponseDTO> cargarMasiva(
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                cargaMasivaUsuarioService.cargarEstudiantesDesdeExcel(userDetails.getIdUsuario(), null, archivo, false)
        );
    }

    @GetMapping("/exportar")
    public void exportar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long idRol,
            @RequestParam(required = false) Boolean activo,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(EXCEL_MEDIA_TYPE.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usuarios_nodo.xlsx\"");
        exportacionUsuarioService.exportarParaCoordinador(
                userDetails.getIdUsuario(), busqueda, idRol, activo, response.getOutputStream()
        );
    }
}
