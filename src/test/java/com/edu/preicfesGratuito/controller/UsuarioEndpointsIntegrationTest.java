package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.UsuarioRequestDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class UsuarioEndpointsIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String obtenerToken(String correo, String password) throws Exception {
        String body = String.format("{\"correo\":\"%s\",\"password\":\"%s\"}", correo, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void registerPublico_retorna403CuandoEstaDeshabilitado() throws Exception {
        UsuarioRequestDTO req = UsuarioRequestDTO.builder()
                .nombres("Test")
                .apellidos("Publico")
                .documento("77777777")
                .correo("publico@preicfes.com")
                .password("123456")
                .idRol(1L)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void flujoCompletoAdminYCoordinador_respetaRolesYNodo() throws Exception {
        String adminToken = obtenerToken("admin@preicfes.com", "123456");
        String directivoToken = obtenerToken("directivo@preicfes.com", "123456");
        String estudianteToken = obtenerToken("estudiante@preicfes.com", "123456");

        // Estudiante no puede entrar a /api/admin ni /api/directivo
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + estudianteToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/directivo/usuarios")
                        .header("Authorization", "Bearer " + estudianteToken))
                .andExpect(status().isForbidden());

        // Directivo no puede entrar a /api/admin/usuarios
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + directivoToken))
                .andExpect(status().isForbidden());

        // Admin lista usuarios paginados y catalogos
        mockMvc.perform(get("/api/admin/usuarios?page=0&size=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.totalElementos").isNumber());

        mockMvc.perform(get("/api/admin/usuarios/catalogos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles.length()").value(4))
                .andExpect(jsonPath("$.nodos").isNotEmpty());

        // Directivo lista usuarios de su nodo y crea profesor en su nodo
        mockMvc.perform(get("/api/directivo/usuarios?page=0&size=10")
                        .header("Authorization", "Bearer " + directivoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());

        // Directivo intenta crear un ADMIN -> 403
        mockMvc.perform(post("/api/directivo/usuarios")
                        .header("Authorization", "Bearer " + directivoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Hack\",\"apellidos\":\"Admin\",\"documento\":\"8888881\",\"correo\":\"hack@preicfes.com\",\"password\":\"123456\",\"idRol\":1}"))
                .andExpect(status().isForbidden());

        // Directivo crea un PROFESOR valido -> 201 y queda vinculado al nodo 1
        MvcResult creadoRes = mockMvc.perform(post("/api/directivo/usuarios")
                        .header("Authorization", "Bearer " + directivoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Sofia\",\"apellidos\":\"Docente\",\"documento\":\"8888882\",\"correo\":\"sofia.docente@preicfes.com\",\"password\":\"123456\",\"idRol\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol.nombre").value("PROFESOR"))
                .andExpect(jsonPath("$.nodo.idNodo").value(1))
                .andReturn();

        long idCreado = objectMapper.readTree(creadoRes.getResponse().getContentAsString()).get("idUsuario").asLong();

        // Directivo inactiva al profesor creado -> 200
        mockMvc.perform(patch("/api/directivo/usuarios/" + idCreado + "/estado")
                        .header("Authorization", "Bearer " + directivoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
