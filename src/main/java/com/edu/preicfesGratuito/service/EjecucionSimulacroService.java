package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.*;
import java.util.List;

public interface EjecucionSimulacroService {
    Long iniciarSimulacro(Long idEstudiante, Long idSimulacro, SimulacroAccesoRequestDTO request);
    List<AreaExamenDTO> obtenerAreasSimulacro(Long idEstudiante, Long idSimulacro);
    List<PreguntaExamenDTO> obtenerPreguntasPorArea(Long idEstudiante, Long idSimulacro, Long idArea);
    void guardarRespuestas(Long idEstudiante, Long idSimulacro, Long idArea, GuardarRespuestasRequestDTO request);
    void finalizarSimulacro(Long idEstudiante, Long idSimulacro);
}
