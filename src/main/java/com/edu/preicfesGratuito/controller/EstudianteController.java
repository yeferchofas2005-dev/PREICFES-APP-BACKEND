package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.EstudianteEstadisticasDTO;
import com.edu.preicfesGratuito.dto.EstudianteResumenDTO;
import com.edu.preicfesGratuito.dto.EstudianteSimulacroDTO;
import com.edu.preicfesGratuito.security.UserDetailsImpl;
import com.edu.preicfesGratuito.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del modulo de estudiante.
 *
 * <p>Todos los endpoints usan el usuario autenticado en el JWT para evitar que
 * un estudiante consulte informacion de otro estudiante enviando IDs externos.</p>
 */
@RestController
@RequestMapping("/api/estudiante")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;

    /**
     * Obtiene el resumen academico del estudiante autenticado.
     *
     * @param userDetails principal cargado desde el token JWT
     * @return cantidad de simulacros realizados, puntaje promedio y tiempo promedio
     */
    @GetMapping("/resumen")
    public ResponseEntity<EstudianteResumenDTO> obtenerResumen(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(estudianteService.obtenerResumen(userDetails.getIdUsuario()));
    }

    /**
     * Lista los simulacros activos del periodo actual asignados al grupo del estudiante.
     *
     * @param userDetails principal cargado desde el token JWT
     * @return simulacros realizados y pendientes con su informacion de resultado
     */
    @GetMapping("/simulacros")
    public ResponseEntity<List<EstudianteSimulacroDTO>> obtenerSimulacrosPeriodoActivo(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(estudianteService.obtenerSimulacrosPeriodoActivo(userDetails.getIdUsuario()));
    }

    /**
     * Retorna los datos agregados necesarios para graficas y analitica del estudiante.
     *
     * @param userDetails principal cargado desde el token JWT
     * @return metricas globales, desempeno por asignatura y resultados por simulacro
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<EstudianteEstadisticasDTO> obtenerEstadisticas(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(estudianteService.obtenerEstadisticas(userDetails.getIdUsuario()));
    }
}
