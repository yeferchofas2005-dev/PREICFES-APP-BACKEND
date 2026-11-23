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
 * Controlador REST para la gestion integral de usuarios por parte del Administrador.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

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
            @RequestParam("idNodo") Long idNodo,
            @RequestParam(value = "encolarEnvio", defaultValue = "false") boolean encolarEnvio,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long idActor = userDetails != null ? userDetails.getIdUsuario() : null;
        return ResponseEntity.ok(
                cargaMasivaUsuarioService.cargarEstudiantesDesdeExcel(idActor, idNodo, archivo, true, encolarEnvio)
        );
    }

    @GetMapping("/exportar")
    public void exportar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long idRol,
            @RequestParam(required = false) Long idNodo,
            @RequestParam(required = false) Boolean activo,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType(EXCEL_MEDIA_TYPE.toString());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usuarios_rumbo_a_la_u.xlsx\"");
        exportacionUsuarioService.exportarParaAdmin(busqueda, idRol, idNodo, activo, response.getOutputStream());
    }
}
