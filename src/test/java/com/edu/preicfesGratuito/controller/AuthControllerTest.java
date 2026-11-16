package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.AuthResponseDTO;
import com.edu.preicfesGratuito.dto.LoginRequestDTO;
import com.edu.preicfesGratuito.dto.RolDTO;
import com.edu.preicfesGratuito.dto.UsuarioResponseDTO;
import com.edu.preicfesGratuito.exception.GlobalExceptionHandler;
import com.edu.preicfesGratuito.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_cuandoCredencialesValidas_retorna200YToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@preicfes.com", "123456");
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-demo")
                .tokenType("Bearer")
                .usuario(UsuarioResponseDTO.builder()
                        .idUsuario(1L)
                        .correo("admin@preicfes.com")
                        .nombres("Administrador")
                        .apellidos("Sistema")
                        .rol(RolDTO.builder().idRol(1L).nombre("ADMIN").build())
                        .build())
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-demo"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol.nombre").value("ADMIN"));
    }

    @Test
    void login_cuandoCamposVacios_retorna400() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.correo").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void login_cuandoCredencialesIncorrectas_retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@preicfes.com", "incorrecta");
        when(authService.login(any())).thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_cuandoUsuarioInactivo_retorna401() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@preicfes.com", "123456");
        when(authService.login(any())).thenThrow(new DisabledException("El usuario se encuentra inactivo"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("El usuario se encuentra inactivo"));
    }
}
