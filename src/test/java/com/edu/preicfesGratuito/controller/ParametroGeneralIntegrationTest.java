package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.config.CacheConfig;
import com.edu.preicfesGratuito.repository.ParametroGeneralRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ParametroGeneralIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private ParametroGeneralRepository parametroGeneralRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        limpiarCaches();
    }

    @AfterEach
    void tearDown() {
        limpiarCaches();
    }

    private void limpiarCaches() {
        for (String cacheName : ListCaches.NAMES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private static final class ListCaches {
        private static final String[] NAMES = {
                CacheConfig.CACHE_PARAMETROS_LISTA,
                CacheConfig.CACHE_PARAMETRO_CLAVE,
                CacheConfig.CACHE_PARAMETRO_ENTERO,
                CacheConfig.CACHE_PARAMETRO_BOOLEANO
        };
    }

    private String obtenerToken(String correo, String password) throws Exception {
        String body = String.format("{\"correo\":\"%s\",\"password\":\"%s\"}", correo, password);
        MvcResult result = mockMvc.perform(postLogin(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postLogin(String body) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    @Test
    void seguridadPorRol_soloAdminPuedeConsultarYModificarParametros() throws Exception {
        mockMvc.perform(get("/api/admin/parametros"))
                .andExpect(status().isUnauthorized());

        String tokenDirectivo = obtenerToken("directivo@preicfes.com", "123456");
        String tokenProfesor = obtenerToken("profesor@preicfes.com", "123456");
        String tokenEstudiante = obtenerToken("estudiante@preicfes.com", "123456");

        for (String token : new String[]{tokenDirectivo, tokenProfesor, tokenEstudiante}) {
            mockMvc.perform(get("/api/admin/parametros")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());

            mockMvc.perform(put("/api/admin/parametros/MAX_NODOS_CONCURRENTES_DIA")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"valor\":\"5\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void cacheEnMemoria_eInvalidacionAlActualizar_funcionanCorrectamente() throws Exception {
        String tokenAdmin = obtenerToken("admin@preicfes.com", "123456");

        // Primera lectura consulta BD y pobla cache
        mockMvc.perform(get("/api/admin/parametros")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clave").exists());

        // Segunda lectura se resuelve desde cache sin volver a consultar el repositorio
        mockMvc.perform(get("/api/admin/parametros")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        verify(parametroGeneralRepository, times(1)).findAllByOrderByClaveAsc();

        // Actualizar parametro invalida cache
        mockMvc.perform(put("/api/admin/parametros/DURACION_SIMULACRO_MINUTOS")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":\"300\",\"descripcion\":\"Simulacro de 5 horas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("300"));

        // Siguiente lectura vuelve a consultar BD y retorna nuevo valor
        mockMvc.perform(get("/api/admin/parametros")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/parametros/DURACION_SIMULACRO_MINUTOS")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("300"));

        verify(parametroGeneralRepository, times(2)).findAllByOrderByClaveAsc();
        assertThatCacheWasPopulated(CacheConfig.CACHE_PARAMETRO_CLAVE, "DURACION_SIMULACRO_MINUTOS");
    }

    private void assertThatCacheWasPopulated(String cacheName, String key) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(cacheName));
        org.assertj.core.api.Assertions.assertThat(cache.get(key)).isNotNull();
    }
}
