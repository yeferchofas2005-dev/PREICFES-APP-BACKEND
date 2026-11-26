package com.edu.preicfesGratuito.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de cache en memoria para datos de lectura frecuente.
 *
 * <p>Usa {@link ConcurrentMapCacheManager} para minimizar consultas repetitivas
 * a MySQL sin introducir dependencias externas adicionales.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PARAMETROS_LISTA = "parametrosGenerales";
    public static final String CACHE_PARAMETRO_CLAVE = "parametroPorClave";
    public static final String CACHE_PARAMETRO_ENTERO = "parametroEnteroPorClave";
    public static final String CACHE_PARAMETRO_BOOLEANO = "parametroBooleanoPorClave";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CACHE_PARAMETROS_LISTA,
                CACHE_PARAMETRO_CLAVE,
                CACHE_PARAMETRO_ENTERO,
                CACHE_PARAMETRO_BOOLEANO
        );
    }
}
