package com.edu.preicfesGratuito.repository;

import com.edu.preicfesGratuito.entity.Grupo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GrupoRepositoryTest {

    @Autowired
    private GrupoRepository grupoRepository;

    @Test
    void findByNodoIdNodoAndActivoTrue_retornaGruposActivosDelNodo() {
        List<Grupo> grupos = grupoRepository.findByNodoIdNodoAndActivoTrue(1L);

        assertThat(grupos).isNotEmpty();
        assertThat(grupos).extracting(Grupo::getNombre).contains("Kennedy-1001");
    }

    @Test
    void findByNodoIdNodoAndActivoTrue_cuandoNodoInexistente_retornaVacio() {
        List<Grupo> grupos = grupoRepository.findByNodoIdNodoAndActivoTrue(9999L);

        assertThat(grupos).isEmpty();
    }
}
