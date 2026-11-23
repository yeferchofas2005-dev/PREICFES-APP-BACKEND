package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.EnvioCredencialItemDTO;
import com.edu.preicfesGratuito.dto.EnvioCredencialResumenDTO;
import com.edu.preicfesGratuito.dto.LoteEnvioResultadoDTO;
import com.edu.preicfesGratuito.dto.PaginaResponseDTO;
import com.edu.preicfesGratuito.enums.EstadoEnvioCredencial;
import com.edu.preicfesGratuito.service.EnvioCredencialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para monitorear y operar la cola global de envio de credenciales (Admin).
 */
@RestController
@RequestMapping("/api/admin/envios-credenciales")
@RequiredArgsConstructor
public class AdminEnvioCredencialController {

    private final EnvioCredencialService envioCredencialService;

    @GetMapping("/resumen")
    public ResponseEntity<EnvioCredencialResumenDTO> obtenerResumen() {
        return ResponseEntity.ok(envioCredencialService.obtenerResumenParaAdmin());
    }

    @GetMapping("/registros")
    public ResponseEntity<PaginaResponseDTO<EnvioCredencialItemDTO>> listarRegistros(
            @RequestParam(required = false) EstadoEnvioCredencial estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(envioCredencialService.listarRegistrosParaAdmin(estado, page, size));
    }

    @PostMapping("/procesar-lote")
    public ResponseEntity<LoteEnvioResultadoDTO> procesarLote() {
        return ResponseEntity.ok(envioCredencialService.iniciarLoteParaAdmin());
    }

    @PostMapping("/reintentar-fallidos")
    public ResponseEntity<LoteEnvioResultadoDTO> reintentarFallidos() {
        return ResponseEntity.ok(envioCredencialService.reintentarFallidosParaAdmin());
    }
}
