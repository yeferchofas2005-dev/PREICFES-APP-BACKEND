package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByCorreo_cuandoUsuarioExiste_retornaUsuarioConRol() {
        Optional<Usuario> resultado = usuarioRepository.findByCorreo("admin@preicfes.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRol().getNombre()).isEqualTo("ADMIN");
        assertThat(resultado.get().getActivo()).isTrue();
    }

    @Test
    void findByCorreo_cuandoNoExiste_retornaEmpty() {
        Optional<Usuario> resultado = usuarioRepository.findByCorreo("noexiste@preicfes.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    void existsByCorreoYDocumento_retornanValoresEsperados() {
        assertThat(usuarioRepository.existsByCorreo("estudiante@preicfes.com")).isTrue();
        assertThat(usuarioRepository.existsByCorreo("inexistente@preicfes.com")).isFalse();
        assertThat(usuarioRepository.existsByDocumento("100000001")).isTrue();
        assertThat(usuarioRepository.existsByDocumento("000000000")).isFalse();
    }
}
