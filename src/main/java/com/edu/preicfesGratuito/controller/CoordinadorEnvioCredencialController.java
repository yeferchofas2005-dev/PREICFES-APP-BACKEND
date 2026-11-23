package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.EnvioCredencialItemDTO;
import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.dto.PaginaResponseDTO;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.EnvioCredencialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para que el Coordinador monitoree y procese envios de su nodo.
 */
@RestController
@RequestMapping("/api/directivo/envios-credenciales")
@RequiredArgsConstructor
public class CoordinadorEnvioCredencialController {

    private final EnvioCredencialService envioCredencialService;

    @GetMapping("/resumen")
    public ResponseEntity<EnvioCredencialResumenDTO> obtenerResumen(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(envioCredencialService.obtenerResumenParaCoordinador(userDetails.getIdUsuario()));
    }

    @GetMapping("/registros")
    public ResponseEntity<PaginaResponseDTO<EnvioCredencialItemDTO>> listarRegistros(
            @RequestParam(required = false) EstadoEnvioCredencial estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                envioCredencialService.listarRegistrosParaCoordinador(userDetails.getIdUsuario(), estado, page, size)
        );
    }

    @PostMapping("/procesar-lote")
    public ResponseEntity<LoteEnvioResultadoDTO> procesarLote(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(envioCredencialService.iniciarLoteParaCoordinador(userDetails.getIdUsuario()));
    }

    @PostMapping("/reintentar-fallidos")
    public ResponseEntity<LoteEnvioResultadoDTO> reintentarFallidos(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(envioCredencialService.reintentarFallidosParaCoordinador(userDetails.getIdUsuario()));
    }
}
