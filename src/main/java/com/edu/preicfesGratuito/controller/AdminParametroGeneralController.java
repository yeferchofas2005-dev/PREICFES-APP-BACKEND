package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.ParametroGeneralActualizarDTO;
import com.edu.preicfesGratuito.dto.ParametroGeneralResponseDTO;
import com.edu.preicfesGratuito.service.ParametroGeneralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la administracion de parametros generales del sistema.
 *
 * <p>Acceso restringido exclusivamente al rol {@code ADMIN}.</p>
 */
@RestController
@RequestMapping("/api/admin/parametros")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminParametroGeneralController {

    private final ParametroGeneralService parametroGeneralService;

    @GetMapping
    public ResponseEntity<List<ParametroGeneralResponseDTO>> listar() {
        return ResponseEntity.ok(parametroGeneralService.listarTodos());
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ParametroGeneralResponseDTO> obtenerPorClave(@PathVariable String clave) {
        return ResponseEntity.ok(parametroGeneralService.obtenerPorClave(clave));
    }

    @PutMapping("/{clave}")
    public ResponseEntity<ParametroGeneralResponseDTO> actualizar(
            @PathVariable String clave,
            @Valid @RequestBody ParametroGeneralActualizarDTO request
    ) {
        return ResponseEntity.ok(parametroGeneralService.actualizar(clave, request));
    }
}
