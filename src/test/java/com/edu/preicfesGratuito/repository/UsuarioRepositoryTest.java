package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Test
    void existsExcludingId_validaUnicidadExcluyendoPropioUsuario() {
        Usuario admin = usuarioRepository.findByCorreo("admin@preicfes.com").orElseThrow();

        assertThat(usuarioRepository.existsByCorreoAndIdUsuarioNot("admin@preicfes.com", admin.getIdUsuario())).isFalse();
        assertThat(usuarioRepository.existsByCorreoAndIdUsuarioNot("estudiante@preicfes.com", admin.getIdUsuario())).isTrue();
        assertThat(usuarioRepository.existsByDocumentoAndIdUsuarioNot("100000001", admin.getIdUsuario())).isFalse();
        assertThat(usuarioRepository.existsByDocumentoAndIdUsuarioNot("100000004", admin.getIdUsuario())).isTrue();
    }

    @Test
    void findByIdWithRolAndNodo_cargaRelacionesSinLazyError() {
        Usuario directivo = usuarioRepository.findByCorreo("directivo@preicfes.com").orElseThrow();

        Optional<Usuario> resultado = usuarioRepository.findByIdWithRolAndNodo(directivo.getIdUsuario());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRol().getNombre()).isEqualTo("DIRECTIVO");
        assertThat(resultado.get().getNodo()).isNotNull();
        assertThat(resultado.get().getNodo().getLocalidad()).isNotNull();
    }

    @Test
    void buscarConFiltros_filtraPorNodoYRolesPermitidosYBusqueda() {
        Page<Usuario> pagina = usuarioRepository.buscarConFiltros(
                "%estudiante%",
                null,
                List.of("PROFESOR", "ESTUDIANTE"),
                1L,
                true,
                PageRequest.of(0, 10)
        );

        assertThat(pagina.getContent()).isNotEmpty();
        assertThat(pagina.getContent())
                .allSatisfy(u -> {
                    assertThat(u.getRol().getNombre()).isIn("PROFESOR", "ESTUDIANTE");
                    assertThat(u.getNodo().getIdNodo()).isEqualTo(1L);
                    assertThat(u.getActivo()).isTrue();
                });
    }

    @Test
    void findDocumentosYCorreosExistentes_retornaSoloCoincidenciasEnLote() {
        List<String> docs = usuarioRepository.findDocumentosExistentes(List.of("100000001", "100000004", "999999999"));
        List<String> correos = usuarioRepository.findCorreosExistentes(List.of("admin@preicfes.com", "noexiste@preicfes.com"));

        assertThat(docs).containsExactlyInAnyOrder("100000001", "100000004");
        assertThat(correos).containsExactly("admin@preicfes.com");
    }
}
