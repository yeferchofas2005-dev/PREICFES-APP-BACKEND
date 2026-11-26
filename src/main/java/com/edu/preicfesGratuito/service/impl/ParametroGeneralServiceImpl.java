package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.config.CacheConfig;
import com.edu.preicfesGratuito.dto.ParametroGeneralActualizarDTO;
import com.edu.preicfesGratuito.dto.ParametroGeneralResponseDTO;
import com.edu.preicfesGratuito.entity.ParametroGeneral;
import com.edu.preicfesGratuito.enums.TipoParametroGeneral;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.ParametroGeneralRepository;
import com.edu.preicfesGratuito.service.ParametroGeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Implementacion del servicio de parametros generales con cache en memoria
 * e invalidacion automatica al actualizar configuraciones.
 */
@Service
@RequiredArgsConstructor
public class ParametroGeneralServiceImpl implements ParametroGeneralService {

    public static final String CLAVE_MAX_NODOS_CONCURRENTES_DIA = "MAX_NODOS_CONCURRENTES_DIA";
    public static final String CLAVE_DURACION_SIMULACRO_MINUTOS = "DURACION_SIMULACRO_MINUTOS";
    public static final String CLAVE_TAMANO_LOTE_RESPUESTAS = "TAMANO_LOTE_RESPUESTAS";
    public static final String CLAVE_MOSTRAR_RETROALIMENTACION_SIMULACRO = "MOSTRAR_RETROALIMENTACION_SIMULACRO";
    public static final String CLAVE_PERMITIR_CODOCENCIA_SALON = "PERMITIR_CODOCENCIA_SALON";
    public static final String CLAVE_JITTER_MAXIMO_CHECKPOINT_MS = "JITTER_MAXIMO_CHECKPOINT_MS";
    public static final String CLAVE_TIEMPO_GRACIA_CIERRE_SEGUNDOS = "TIEMPO_GRACIA_CIERRE_SEGUNDOS";
    public static final String CLAVE_VENTANA_ACTIVACION_PROFESOR_MINUTOS = "VENTANA_ACTIVACION_PROFESOR_MINUTOS";
    public static final String CLAVE_CACHE_SIMULACROS_TTL_MINUTOS = "CACHE_SIMULACROS_TTL_MINUTOS";
    public static final String CLAVE_MAX_CORREOS_POR_DIA = "MAX_CORREOS_POR_DIA";
    public static final String CLAVE_CORREO_DELAY_ENTRE_ENVIOS_MS = "CORREO_DELAY_ENTRE_ENVIOS_MS";

    private static final Set<String> CLAVES_ENTEROS_POSITIVOS = Set.of(
            CLAVE_MAX_NODOS_CONCURRENTES_DIA,
            CLAVE_DURACION_SIMULACRO_MINUTOS,
            CLAVE_TAMANO_LOTE_RESPUESTAS,
            CLAVE_CACHE_SIMULACROS_TTL_MINUTOS,
            CLAVE_MAX_CORREOS_POR_DIA
    );

    private static final Set<String> CLAVES_ENTEROS_NO_NEGATIVOS = Set.of(
            CLAVE_JITTER_MAXIMO_CHECKPOINT_MS,
            CLAVE_TIEMPO_GRACIA_CIERRE_SEGUNDOS,
            CLAVE_VENTANA_ACTIVACION_PROFESOR_MINUTOS,
            CLAVE_CORREO_DELAY_ENTRE_ENVIOS_MS
    );

    private final ParametroGeneralRepository parametroGeneralRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_PARAMETROS_LISTA)
    public List<ParametroGeneralResponseDTO> listarTodos() {
        return parametroGeneralRepository.findAllByOrderByClaveAsc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.CACHE_PARAMETRO_CLAVE,
            key = "#clave != null ? #clave.trim().toUpperCase(T(java.util.Locale).ROOT) : ''"
    )
    public ParametroGeneralResponseDTO obtenerPorClave(String clave) {
        String claveNormalizada = normalizarClave(clave);
        ParametroGeneral parametro = parametroGeneralRepository.findByClave(claveNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro general no encontrado: " + claveNormalizada));
        return toResponseDTO(parametro);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.CACHE_PARAMETRO_ENTERO,
            key = "#clave != null ? #clave.trim().toUpperCase(T(java.util.Locale).ROOT) : ''"
    )
    public int obtenerValorEntero(String clave, int valorPorDefecto) {
        String claveNormalizada = normalizarClave(clave);
        return parametroGeneralRepository.findByClave(claveNormalizada)
                .map(parametro -> {
                    try {
                        return Integer.parseInt(parametro.getValor().trim());
                    } catch (NumberFormatException ex) {
                        return valorPorDefecto;
                    }
                })
                .orElse(valorPorDefecto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.CACHE_PARAMETRO_BOOLEANO,
            key = "#clave != null ? #clave.trim().toUpperCase(T(java.util.Locale).ROOT) : ''"
    )
    public boolean obtenerValorBooleano(String clave, boolean valorPorDefecto) {
        String claveNormalizada = normalizarClave(clave);
        return parametroGeneralRepository.findByClave(claveNormalizada)
                .map(parametro -> {
                    String val = parametro.getValor().trim();
                    if ("true".equalsIgnoreCase(val)) {
                        return true;
                    }
                    if ("false".equalsIgnoreCase(val)) {
                        return false;
                    }
                    return valorPorDefecto;
                })
                .orElse(valorPorDefecto);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    CacheConfig.CACHE_PARAMETROS_LISTA,
                    CacheConfig.CACHE_PARAMETRO_CLAVE,
                    CacheConfig.CACHE_PARAMETRO_ENTERO,
                    CacheConfig.CACHE_PARAMETRO_BOOLEANO
            },
            allEntries = true
    )
    public ParametroGeneralResponseDTO actualizar(String clave, ParametroGeneralActualizarDTO request) {
        String claveNormalizada = normalizarClave(clave);
        ParametroGeneral parametro = parametroGeneralRepository.findByClave(claveNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro general no encontrado: " + claveNormalizada));

        String valorValidado = validarYNormalizarValor(claveNormalizada, parametro.getTipo(), request.getValor());
        parametro.setValor(valorValidado);

        if (request.getDescripcion() != null) {
            String desc = request.getDescripcion().trim();
            parametro.setDescripcion(desc.isEmpty() ? null : desc);
        }

        ParametroGeneral actualizado = parametroGeneralRepository.saveAndFlush(parametro);
        return toResponseDTO(actualizado);
    }

    private String normalizarClave(String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new BadRequestException("La clave del parámetro es obligatoria");
        }
        return clave.trim().toUpperCase(Locale.ROOT);
    }

    private String validarYNormalizarValor(String clave, TipoParametroGeneral tipo, String rawValor) {
        if (rawValor == null || rawValor.trim().isEmpty()) {
            throw new BadRequestException("El valor del parámetro es obligatorio");
        }
        String trimmed = rawValor.trim();

        return switch (tipo) {
            case INTEGER -> validarEntero(clave, trimmed);
            case DECIMAL -> validarDecimal(clave, trimmed);
            case BOOLEAN -> validarBooleano(clave, trimmed);
            case STRING -> {
                if (trimmed.length() > 255) {
                    throw new BadRequestException("El valor de " + clave + " no puede superar los 255 caracteres");
                }
                yield trimmed;
            }
        };
    }

    private String validarEntero(String clave, String valor) {
        final int numero;
        try {
            numero = Integer.parseInt(valor);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("El parámetro " + clave + " requiere un número entero válido");
        }
        if (CLAVES_ENTEROS_POSITIVOS.contains(clave) && numero < 1) {
            throw new BadRequestException("El parámetro " + clave + " debe ser mayor o igual a 1");
        }
        if (CLAVES_ENTEROS_NO_NEGATIVOS.contains(clave) && numero < 0) {
            throw new BadRequestException("El parámetro " + clave + " debe ser mayor o igual a 0");
        }
        return String.valueOf(numero);
    }

    private String validarDecimal(String clave, String valor) {
        try {
            new BigDecimal(valor);
            return valor;
        } catch (NumberFormatException ex) {
            throw new BadRequestException("El parámetro " + clave + " requiere un valor decimal válido");
        }
    }

    private String validarBooleano(String clave, String valor) {
        if ("true".equalsIgnoreCase(valor) || "false".equalsIgnoreCase(valor)) {
            return valor.toLowerCase(Locale.ROOT);
        }
        throw new BadRequestException("El parámetro " + clave + " requiere un valor booleano (true o false)");
    }

    private ParametroGeneralResponseDTO toResponseDTO(ParametroGeneral entity) {
        return ParametroGeneralResponseDTO.builder()
                .idParametro(entity.getIdParametro())
                .clave(entity.getClave())
                .valor(entity.getValor())
                .descripcion(entity.getDescripcion())
                .tipo(entity.getTipo())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }
}
