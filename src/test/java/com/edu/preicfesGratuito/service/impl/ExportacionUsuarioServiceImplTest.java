package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.entity.Localidad;
import com.edu.preicfesGratuito.entity.Nodo;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.repository.UsuarioExportacionProjection;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportacionUsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ExportacionUsuarioServiceImpl service;

    @Test
    void exportarParaAdmin_generaExcelConLas9ColumnasEsperadasSinPassword() throws Exception {
        UsuarioExportacionProjection fila = crearProyeccion(
                "Maria", "Estudiante", "100000004", "estudiante@preicfes.com",
                "3001112233", "ESTUDIANTE", "Kennedy Central", "Kennedy-1001", true
        );

        when(usuarioRepository.findParaExportacion(eq("%maria%"), isNull(), anyList(), eq(1L), eq(true)))
                .thenReturn(List.of(fila));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.exportarParaAdmin("Maria", null, 1L, true, out);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheet("Usuarios");
            assertThat(sheet).isNotNull();
            Row header = sheet.getRow(0);
            assertThat(header.getLastCellNum()).isEqualTo((short) 9);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("nombres");
            assertThat(header.getCell(5).getStringCellValue()).isEqualTo("rol");
            assertThat(header.getCell(7).getStringCellValue()).isEqualTo("grupo_salon");
            assertThat(header.getCell(8).getStringCellValue()).isEqualTo("activo");

            Row row = sheet.getRow(1);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Maria");
            assertThat(row.getCell(7).getStringCellValue()).isEqualTo("Kennedy-1001");
            assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Activo");
        }
    }

    @Test
    void exportarParaCoordinador_filtraPorNodoYRolesPermitidos() {
        Nodo nodo = Nodo.builder()
                .idNodo(1L)
                .nombre("Kennedy Central")
                .localidad(Localidad.builder().idLocalidad(1L).nombre("KENNEDY").build())
                .build();
        Usuario coord = Usuario.builder()
                .idUsuario(2L)
                .rol(Rol.builder().idRol(2L).nombre("DIRECTIVO").build())
                .nodo(nodo)
                .build();

        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coord));
        when(usuarioRepository.findParaExportacion(isNull(), isNull(), eq(List.of("PROFESOR", "ESTUDIANTE")), eq(1L), isNull()))
                .thenReturn(List.of());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.exportarParaCoordinador(2L, null, null, null, out);

        assertThat(out.toByteArray()).isNotEmpty();
        verify(usuarioRepository).findParaExportacion(
                isNull(), isNull(), eq(List.of("PROFESOR", "ESTUDIANTE")), eq(1L), isNull()
        );
    }

    @Test
    void exportarParaCoordinador_cuandoSinNodo_lanzaBadRequest() {
        Usuario coordSinNodo = Usuario.builder().idUsuario(2L).nodo(null).build();
        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordSinNodo));

        assertThatThrownBy(() -> service.exportarParaCoordinador(2L, null, null, null, new ByteArrayOutputStream()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nodo");
    }

    private UsuarioExportacionProjection crearProyeccion(
            String nombres, String apellidos, String documento, String correo,
            String telefono, String rol, String nodo, String grupoSalon, Boolean activo
    ) {
        return new UsuarioExportacionProjection() {
            @Override public String getNombres() { return nombres; }
            @Override public String getApellidos() { return apellidos; }
            @Override public String getDocumento() { return documento; }
            @Override public String getCorreo() { return correo; }
            @Override public String getTelefono() { return telefono; }
            @Override public String getRol() { return rol; }
            @Override public String getNodo() { return nodo; }
            @Override public String getGrupoSalon() { return grupoSalon; }
            @Override public Boolean getActivo() { return activo; }
        };
    }
}
