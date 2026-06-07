package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.AsignaturaDesempenoDTO;
import com.edu.preicfesGratuito.dto.AsignaturaTiempoDTO;
import com.edu.preicfesGratuito.dto.EstudianteEstadisticasDTO;
import com.edu.preicfesGratuito.dto.EstudianteResumenDTO;
import com.edu.preicfesGratuito.dto.EstudianteSimulacroDTO;
import com.edu.preicfesGratuito.dto.ResultadoSimulacroAsignaturaDTO;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.EstudianteEstadisticasGeneralesProjection;
import com.edu.preicfesGratuito.repository.EstudianteResumenProjection;
import com.edu.preicfesGratuito.repository.IntentoSimulacroRepository;
import com.edu.preicfesGratuito.repository.SimulacroRepository;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementacion de consultas y estadisticas para estudiantes.
 *
 * <p>La capa delega los calculos pesados a consultas agregadas en base de datos
 * para evitar cargar grandes volumenes de respuestas en memoria.</p>
 */
@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements EstudianteService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final UsuarioRepository usuarioRepository;
    private final IntentoSimulacroRepository intentoSimulacroRepository;
    private final SimulacroRepository simulacroRepository;

    /**
     * Obtiene el resumen principal del estudiante.
     *
     * @param idEstudiante identificador del estudiante autenticado
     * @return resumen con promedios calculados desde intentos finalizados
     */
    @Override
    @Transactional(readOnly = true)
    public EstudianteResumenDTO obtenerResumen(Long idEstudiante) {
        validarEstudianteExiste(idEstudiante);
        EstudianteResumenProjection resumen = intentoSimulacroRepository.findResumenByEstudianteId(idEstudiante);

        return EstudianteResumenDTO.builder()
                .idEstudiante(resumen.getIdEstudiante())
                .nombres(resumen.getNombres())
                .apellidos(resumen.getApellidos())
                .simulacrosRealizados(resumen.getSimulacrosRealizados())
                .puntajePromedio(valueOrZero(resumen.getPuntajePromedio()))
                .tiempoPromedioSegundos(valueOrZero(resumen.getTiempoPromedioSegundos()))
                .build();
    }

    /**
     * Obtiene los simulacros visibles para el estudiante en el periodo activo.
     *
     * @param idEstudiante identificador del estudiante autenticado
     * @return simulacros asignados al grupo del estudiante con estado de realizacion
     */
    @Override
    @Transactional(readOnly = true)
    public List<EstudianteSimulacroDTO> obtenerSimulacrosPeriodoActivo(Long idEstudiante) {
        validarEstudianteExiste(idEstudiante);

        return simulacroRepository.findSimulacrosPeriodoActivoByEstudianteId(idEstudiante)
                .stream()
                .map(simulacro -> EstudianteSimulacroDTO.builder()
                        .idSimulacro(simulacro.getIdSimulacro())
                        .nombre(simulacro.getNombre())
                        .fecha(simulacro.getFecha())
                        .estado(simulacro.getEstado())
                        .puntaje(simulacro.getPuntaje())
                        .tiempoSegundos(simulacro.getTiempoSegundos())
                        .porcentajeAprobacion(simulacro.getPorcentajeAprobacion())
                        .build())
                .toList();
    }

    /**
     * Consolida las estadisticas necesarias para el dashboard del estudiante.
     *
     * @param idEstudiante identificador del estudiante autenticado
     * @return datos globales y series para graficas por asignatura y simulacro
     */
    @Override
    @Transactional(readOnly = true)
    public EstudianteEstadisticasDTO obtenerEstadisticas(Long idEstudiante) {
        validarEstudianteExiste(idEstudiante);
        EstudianteEstadisticasGeneralesProjection generales =
                intentoSimulacroRepository.findEstadisticasGeneralesByEstudianteId(idEstudiante);

        List<AsignaturaDesempenoDTO> desempenoPorAsignatura =
                intentoSimulacroRepository.findDesempenoPorAsignaturaByEstudianteId(idEstudiante)
                        .stream()
                        .map(asignatura -> AsignaturaDesempenoDTO.builder()
                                .idArea(asignatura.getIdArea())
                                .asignatura(asignatura.getAsignatura())
                                .puntajePromedio(valueOrZero(asignatura.getPuntajePromedio()))
                                .build())
                        .toList();

        List<AsignaturaTiempoDTO> tiempoPorAsignatura =
                intentoSimulacroRepository.findTiempoPorAsignaturaByEstudianteId(idEstudiante)
                        .stream()
                        .map(asignatura -> AsignaturaTiempoDTO.builder()
                                .idArea(asignatura.getIdArea())
                                .asignatura(asignatura.getAsignatura())
                                .tiempoPromedioSegundos(valueOrZero(asignatura.getTiempoPromedioSegundos()))
                                .build())
                        .toList();

        List<ResultadoSimulacroAsignaturaDTO> resultadoSimulacro =
                intentoSimulacroRepository.findResultadosPorSimulacroYAsignaturaByEstudianteId(idEstudiante)
                        .stream()
                        .map(resultado -> ResultadoSimulacroAsignaturaDTO.builder()
                                .idSimulacro(resultado.getIdSimulacro())
                                .simulacro(resultado.getSimulacro())
                                .idArea(resultado.getIdArea())
                                .asignatura(resultado.getAsignatura())
                                .puntaje(valueOrZero(resultado.getPuntaje()))
                                .build())
                        .toList();

        return EstudianteEstadisticasDTO.builder()
                .puntajeGlobal(valueOrZero(generales.getPuntajeGlobal()))
                .porcentajeNacional(valueOrZero(generales.getPorcentajeNacional()))
                .tiempoPromedioSegundos(valueOrZero(generales.getTiempoPromedioSegundos()))
                .desempenoPorAsignatura(desempenoPorAsignatura)
                .tiempoPorAsignatura(tiempoPorAsignatura)
                .resultadoSimulacro(resultadoSimulacro)
                .build();
    }

    /**
     * Verifica que el estudiante exista antes de ejecutar consultas de negocio.
     *
     * @param idEstudiante identificador a validar
     */
    private void validarEstudianteExiste(Long idEstudiante) {
        if (!usuarioRepository.existsById(idEstudiante)) {
            throw new ResourceNotFoundException("Estudiante no encontrado con id: " + idEstudiante);
        }
    }

    /**
     * Normaliza valores numericos nulos que pueden venir de agregaciones SQL sin datos.
     *
     * @param value valor recibido desde la proyeccion
     * @return el valor original o cero cuando es nulo
     */
    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
