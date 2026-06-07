package com.edu.preicfesGratuito.controller;

import com.edu.preicfesGratuito.dto.AuthResponseDTO;
import com.edu.preicfesGratuito.dto.LoginRequestDTO;
import com.edu.preicfesGratuito.dto.UsuarioRequestDTO;
import com.edu.preicfesGratuito.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST encargado de las operaciones publicas de autenticacion.
 *
 * <p>Estos endpoints no requieren token JWT y son el punto de entrada para
 * registrar usuarios y obtener tokens de acceso.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Autentica un usuario por correo y contrasena.
     *
     * @param request credenciales enviadas por el cliente
     * @return token JWT y datos basicos del usuario autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Registra un nuevo usuario y devuelve un token JWT para iniciar sesion.
     *
     * @param request datos requeridos para crear el usuario
     * @return token JWT y datos del usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
