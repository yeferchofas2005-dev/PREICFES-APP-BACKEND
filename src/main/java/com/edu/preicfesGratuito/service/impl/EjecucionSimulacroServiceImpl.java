package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.entity.*;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.*;
import com.edu.preicfesGratuito.service.EjecucionSimulacroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EjecucionSimulacroServiceImpl implements EjecucionSimulacroService {

    private final IntentoSimulacroRepository intentoSimulacroRepository;
    private final GrupoEstudianteRepository grupoEstudianteRepository;
    private final SimulacroGrupoRepository simulacroGrupoRepository;
    private final SimulacroRepository simulacroRepository;
    private final UsuarioRepository usuarioRepository;
    private final AreaRepository areaRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaEstudianteRepository respuestaEstudianteRepository;
    private final EstadisticaGrupoRepository estadisticaGrupoRepository;
    private final OpcionRespuestaRepository opcionRespuestaRepository;

    @Override
    @Transactional
    public Long iniciarSimulacro(Long idEstudiante, Long idSimulacro, SimulacroAccesoRequestDTO request) {
        GrupoEstudiante ge = grupoEstudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante no pertenece a ningún grupo"));

        SimulacroGrupo sg = simulacroGrupoRepository.findById(new SimulacroGrupoId(idSimulacro, ge.getGrupo().getIdGrupo()))
                .orElseThrow(() -> new ResourceNotFoundException("El simulacro no está asignado al grupo del estudiante"));

        if (!sg.getCodigoAcceso().equals(request.getCodigoAcceso())) {
            throw new IllegalArgumentException("Código de acceso incorrecto");
        }

        Optional<IntentoSimulacro> intentoExistente = intentoSimulacroRepository
                .findByEstudianteIdUsuarioAndSimulacroIdSimulacro(idEstudiante, idSimulacro);

        if (intentoExistente.isPresent()) {
            if (intentoExistente.get().getFechaFin() != null) {
                throw new IllegalStateException("El simulacro ya ha sido finalizado");
            }
            return intentoExistente.get().getIdIntento();
        }

        Usuario estudiante = usuarioRepository.findById(idEstudiante).orElseThrow();
        Simulacro simulacro = simulacroRepository.findById(idSimulacro).orElseThrow();

        IntentoSimulacro nuevoIntento = IntentoSimulacro.builder()
                .estudiante(estudiante)
                .simulacro(simulacro)
                .fechaInicio(LocalDateTime.now())
                .build();

        return intentoSimulacroRepository.save(nuevoIntento).getIdIntento();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaExamenDTO> obtenerAreasSimulacro(Long idEstudiante, Long idSimulacro) {
        validarIntentoActivo(idEstudiante, idSimulacro);
        return areaRepository.findAreasBySimulacro(idSimulacro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreguntaExamenDTO> obtenerPreguntasPorArea(Long idEstudiante, Long idSimulacro, Long idArea) {
        validarIntentoActivo(idEstudiante, idSimulacro);

        List<Pregunta> preguntas = preguntaRepository.findPreguntasBySimulacroAndArea(idSimulacro, idArea);
        if (preguntas.isEmpty()) {
            return List.of();
        }

        List<Long> idPreguntas = preguntas.stream().map(Pregunta::getIdPregunta).toList();
        List<OpcionRespuesta> todasOpciones = opcionRespuestaRepository.findByPreguntaIdPreguntaIn(idPreguntas);

        return preguntas.stream().map(p -> {
            PreguntaExamenDTO dto = PreguntaExamenDTO.builder()
                    .idPregunta(p.getIdPregunta())
                    .enunciado(p.getEnunciado())
                    .rutaImagen(p.getRutaImagen())
                    .nivelDificultad(p.getNivelDificultad())
                    .build();

            if (p.getCaso() != null) {
                dto.setCaso(CasoExamenDTO.builder()
                        .idCaso(p.getCaso().getIdCaso())
                        .titulo(p.getCaso().getTitulo())
                        .textoBase(p.getCaso().getTextoBase())
                        .rutaImagen(p.getCaso().getRutaImagen())
                        .build());
            }

            List<OpcionExamenDTO> opcionesDto = todasOpciones.stream()
                    .filter(o -> o.getPregunta().getIdPregunta().equals(p.getIdPregunta()))
                    .map(o -> OpcionExamenDTO.builder()
                            .idOpcion(o.getIdOpcion())
                            .texto(o.getTexto())
                            .build())
                    .toList();
            
            dto.setOpciones(opcionesDto);

            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public void guardarRespuestas(Long idEstudiante, Long idSimulacro, Long idArea, GuardarRespuestasRequestDTO request) {
        IntentoSimulacro intento = validarIntentoActivo(idEstudiante, idSimulacro);
        if (request.getRespuestas() == null || request.getRespuestas().isEmpty()) {
            return;
        }

        List<Long> idPreguntas = request.getRespuestas().stream()
                .map(RespuestaItemDTO::getIdPregunta)
                .distinct()
                .toList();

        Map<Long, RespuestaEstudiante> existentes = respuestaEstudianteRepository
                .findByIntentoIdIntentoAndPreguntaIdPreguntaIn(intento.getIdIntento(), idPreguntas)
                .stream()
                .collect(Collectors.toMap(
                        re -> re.getPregunta().getIdPregunta(),
                        Function.identity()
                ));

        List<RespuestaEstudiante> respuestasAGuardar = request.getRespuestas().stream().map(r -> {
            OpcionRespuesta opcion = opcionRespuestaRepository.getReferenceById(r.getIdOpcion());
            RespuestaEstudiante existente = existentes.get(r.getIdPregunta());

            if (existente != null) {
                existente.setOpcion(opcion);
                existente.setTiempoRespuestaSegundos(r.getTiempoRespuestaSegundos());
                return existente;
            }

            Pregunta pregunta = preguntaRepository.getReferenceById(r.getIdPregunta());
            return RespuestaEstudiante.builder()
                    .intento(intento)
                    .pregunta(pregunta)
                    .opcion(opcion)
                    .tiempoRespuestaSegundos(r.getTiempoRespuestaSegundos())
                    .build();
        }).toList();

        // DB triggers (BEFORE INSERT / BEFORE UPDATE) calculate 'es_correcta'
        respuestaEstudianteRepository.saveAll(respuestasAGuardar);
    }

    @Override
    @Transactional
    public void finalizarSimulacro(Long idEstudiante, Long idSimulacro) {
        IntentoSimulacro intento = validarIntentoActivo(idEstudiante, idSimulacro);

        // Call stored procedure to finalize
        intentoSimulacroRepository.spFinalizarIntento(intento.getIdIntento());

        // Update group statistics
        GrupoEstudiante ge = grupoEstudianteRepository.findById(idEstudiante).orElseThrow();
        estadisticaGrupoRepository.spActualizarEstadisticaGrupo(ge.getGrupo().getIdGrupo());
    }

    private IntentoSimulacro validarIntentoActivo(Long idEstudiante, Long idSimulacro) {
        IntentoSimulacro intento = intentoSimulacroRepository
                .findByEstudianteIdUsuarioAndSimulacroIdSimulacro(idEstudiante, idSimulacro)
                .orElseThrow(() -> new IllegalStateException("No has iniciado este simulacro"));

        if (intento.getFechaFin() != null) {
            throw new IllegalStateException("Este simulacro ya ha sido finalizado");
        }
        return intento;
    }
}
