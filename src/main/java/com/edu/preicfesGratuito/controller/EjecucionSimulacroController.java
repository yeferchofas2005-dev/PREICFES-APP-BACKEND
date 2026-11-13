package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.EjecucionSimulacroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la ejecución de simulacros por parte de los estudiantes.
 */
@RestController
@RequestMapping("/api/estudiante/ejecucion")
@RequiredArgsConstructor
public class EjecucionSimulacroController {

    private final EjecucionSimulacroService ejecucionSimulacroService;

    /**
     * Inicia un simulacro validando el código de acceso.
     */
    @PostMapping("/{idSimulacro}/iniciar")
    public ResponseEntity<Long> iniciarSimulacro(
            @PathVariable Long idSimulacro,
            @Valid @RequestBody SimulacroAccesoRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long idIntento = ejecucionSimulacroService.iniciarSimulacro(userDetails.getIdUsuario(), idSimulacro, request);
        return ResponseEntity.ok(idIntento);
    }

    /**
     * Obtiene la lista de áreas que componen el simulacro.
     */
    @GetMapping("/{idSimulacro}/areas")
    public ResponseEntity<List<AreaExamenDTO>> obtenerAreas(
            @PathVariable Long idSimulacro,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ejecucionSimulacroService.obtenerAreasSimulacro(userDetails.getIdUsuario(), idSimulacro));
    }

    /**
     * Obtiene las preguntas de una área específica.
     */
    @GetMapping("/{idSimulacro}/areas/{idArea}/preguntas")
    public ResponseEntity<List<PreguntaExamenDTO>> obtenerPreguntasPorArea(
            @PathVariable Long idSimulacro,
            @PathVariable Long idArea,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(ejecucionSimulacroService.obtenerPreguntasPorArea(userDetails.getIdUsuario(), idSimulacro, idArea));
    }

    /**
     * Guarda las respuestas enviadas para una área.
     */
    @PostMapping("/{idSimulacro}/areas/{idArea}/respuestas")
    public ResponseEntity<Void> guardarRespuestasArea(
            @PathVariable Long idSimulacro,
            @PathVariable Long idArea,
            @Valid @RequestBody GuardarRespuestasRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ejecucionSimulacroService.guardarRespuestas(userDetails.getIdUsuario(), idSimulacro, idArea, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Finaliza el intento y calcula puntajes.
     */
    @PostMapping("/{idSimulacro}/finalizar")
    public ResponseEntity<Void> finalizarSimulacro(
            @PathVariable Long idSimulacro,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ejecucionSimulacroService.finalizarSimulacro(userDetails.getIdUsuario(), idSimulacro);
        return ResponseEntity.ok().build();
    }
}
