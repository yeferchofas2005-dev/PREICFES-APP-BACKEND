package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.CargaMasivaUsuariosResponseDTO;
import com.edu.preicfesGratuito.entity.*;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.repository.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargaMasivaUsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private NodoRepository nodoRepository;
    @Mock
    private GrupoRepository grupoRepository;
    @Mock
    private GrupoEstudianteRepository grupoEstudianteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CargaMasivaUsuarioServiceImpl service;

    private Nodo nodoKennedy;
    private Rol rolEstudiante;
    private Grupo grupoKennedy;
    private Usuario coordinadorKennedy;

    @BeforeEach
    void setUp() {
        nodoKennedy = Nodo.builder().idNodo(1L).nombre("Kennedy Central").activo(true).build();
        rolEstudiante = Rol.builder().idRol(4L).nombre("ESTUDIANTE").build();
        grupoKennedy = Grupo.builder().idGrupo(1L).nombre("Kennedy-1001").nodo(nodoKennedy).activo(true).build();
        coordinadorKennedy = Usuario.builder()
                .idUsuario(2L)
                .nodo(nodoKennedy)
                .rol(Rol.builder().idRol(2L).nombre("DIRECTIVO").build())
                .build();
    }

    @Test
    void generarPlantillaExcel_retornaLibroXlsxValidoConEncabezados() throws Exception {
        byte[] bytes = service.generarPlantillaExcel();

        assertThat(bytes).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("nombres");
            assertThat(header.getCell(2).getStringCellValue()).isEqualTo("documento");
            assertThat(header.getCell(5).getStringCellValue()).isEqualTo("grupo_salon");
        }
    }

    @Test
    void cargarEstudiantes_conFilasValidasYErrores_procesaLoteSinAbortar() throws Exception {
        byte[] excelBytes = crearExcelPrueba(List.of(
                new String[]{"Laura", "Perez", "20001", "laura@preicfes.com", "3001112233", "Kennedy-1001", ""},
                new String[]{"Camilo", "Rojas", "20002", "camilo@preicfes.com", "", "", "Clave123"},
                new String[]{"Duplicado", "Archivo", "20001", "dup@preicfes.com", "", "", ""},
                new String[]{"Existente", "BD", "100000004", "existente@preicfes.com", "", "", ""},
                new String[]{"Grupo", "Malo", "20003", "grupomalo@preicfes.com", "", "NoExiste-999", ""},
                new String[]{"Correo", "Malo", "20004", "correo-invalido", "", "", ""}
        ));

        MockMultipartFile file = new MockMultipartFile(
                "archivo", "estudiantes.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        when(nodoRepository.findById(1L)).thenReturn(Optional.of(nodoKennedy));
        when(rolRepository.findByNombre("ESTUDIANTE")).thenReturn(Optional.of(rolEstudiante));
        when(grupoRepository.findByNodoIdNodoAndActivoTrue(1L)).thenReturn(List.of(grupoKennedy));
        when(usuarioRepository.findDocumentosExistentes(anyCollection())).thenReturn(List.of("100000004"));
        when(usuarioRepository.findCorreosExistentes(anyCollection())).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(usuarioRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> {
            List<Usuario> lista = inv.getArgument(0);
            long seq = 100L;
            for (Usuario u : lista) {
                u.setIdUsuario(seq++);
            }
            return lista;
        });

        CargaMasivaUsuariosResponseDTO res = service.cargarEstudiantesDesdeExcel(1L, 1L, file, true);

        assertThat(res.getTotalFilasProcesadas()).isEqualTo(6);
        assertThat(res.getExitosos()).isEqualTo(2);
        assertThat(res.getFallidos()).isEqualTo(4);
        assertThat(res.getVinculadosAGrupo()).isEqualTo(1);
        assertThat(res.getErrores()).hasSize(4);
        verify(passwordEncoder).encode("20001");
        verify(passwordEncoder).encode("Clave123");
        verify(grupoEstudianteRepository).saveAllAndFlush(argThat(iterable -> {
            int count = 0;
            for (Object ignored : iterable) count++;
            return count == 1;
        }));
    }

    @Test
    void cargarEstudiantes_cuandoCoordinador_usaNodoPropioDelCoordinador() throws Exception {
        byte[] excelBytes = crearExcelPrueba(List.<String[]>of(
                new String[]{"Sofia", "Diaz", "30001", "sofia@preicfes.com", "", "Kennedy-1001", ""}
        ));
        MockMultipartFile file = new MockMultipartFile("archivo", "est.xlsx", "application/octet-stream", excelBytes);

        when(usuarioRepository.findByIdWithRolAndNodo(2L)).thenReturn(Optional.of(coordinadorKennedy));
        when(rolRepository.findByNombre("ESTUDIANTE")).thenReturn(Optional.of(rolEstudiante));
        when(grupoRepository.findByNodoIdNodoAndActivoTrue(1L)).thenReturn(List.of(grupoKennedy));
        when(usuarioRepository.findDocumentosExistentes(anyCollection())).thenReturn(List.of());
        when(usuarioRepository.findCorreosExistentes(anyCollection())).thenReturn(List.of());
        when(passwordEncoder.encode("30001")).thenReturn("$2a$10$hash");
        when(usuarioRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> {
            List<Usuario> lista = inv.getArgument(0);
            lista.get(0).setIdUsuario(200L);
            return lista;
        });

        CargaMasivaUsuariosResponseDTO res = service.cargarEstudiantesDesdeExcel(2L, null, file, false);

        assertThat(res.getExitosos()).isEqualTo(1);
        assertThat(res.getVinculadosAGrupo()).isEqualTo(1);
        verifyNoInteractions(nodoRepository);
    }

    @Test
    void cargarEstudiantes_cuandoExtensionInvalida_lanzaBadRequest() {
        MockMultipartFile file = new MockMultipartFile("archivo", "datos.csv", "text/csv", "a,b".getBytes());

        assertThatThrownBy(() -> service.cargarEstudiantesDesdeExcel(1L, 1L, file, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(".xlsx");
    }

    @Test
    void cargarEstudiantes_cuandoAdminSinNodo_lanzaBadRequest() throws Exception {
        byte[] excelBytes = crearExcelPrueba(List.<String[]>of(new String[]{"A", "B", "1", "a@b.com", "", "", ""}));
        MockMultipartFile file = new MockMultipartFile("archivo", "datos.xlsx", "application/octet-stream", excelBytes);

        assertThatThrownBy(() -> service.cargarEstudiantesDesdeExcel(1L, null, file, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nodo");
    }

    private byte[] crearExcelPrueba(List<String[]> filas) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Estudiantes");
            Row header = sheet.createRow(0);
            String[] cols = {"nombres", "apellidos", "documento", "correo", "telefono", "grupo_salon", "password"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            for (int r = 0; r < filas.size(); r++) {
                Row row = sheet.createRow(r + 1);
                String[] vals = filas.get(r);
                for (int c = 0; c < vals.length; c++) {
                    row.createCell(c).setCellValue(vals[c]);
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }
}
