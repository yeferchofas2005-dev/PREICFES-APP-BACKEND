package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.RolNombre;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.UsuarioExportacionProjection;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.service.ExportacionUsuarioService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Implementacion streaming de exportacion de usuarios a Excel (.xlsx) mediante SXSSFWorkbook.
 */
@Service
@RequiredArgsConstructor
public class ExportacionUsuarioServiceImpl implements ExportacionUsuarioService {

    private static final int STREAMING_WINDOW_SIZE = 100;
    private static final List<String> TODOS_LOS_ROLES = List.of(
            RolNombre.ADMIN.name(),
            RolNombre.DIRECTIVO.name(),
            RolNombre.PROFESOR.name(),
            RolNombre.ESTUDIANTE.name()
    );
    private static final List<String> ROLES_COORDINADOR = List.of(
            RolNombre.PROFESOR.name(),
            RolNombre.ESTUDIANTE.name()
    );
    private static final List<String> COLUMNAS = List.of(
            "nombres", "apellidos", "documento", "correo", "telefono", "rol", "nodo", "grupo_salon", "activo"
    );
    private static final int[] ANCHOS_COLUMNAS = {22, 22, 16, 30, 16, 15, 22, 22, 12};

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public void exportarParaAdmin(
            String busqueda, Long idRol, Long idNodo, Boolean activo, OutputStream outputStream
    ) {
        String pattern = normalizarBusqueda(busqueda);
        List<UsuarioExportacionProjection> filas = usuarioRepository.findParaExportacion(
                pattern, idRol, TODOS_LOS_ROLES, idNodo, activo
        );
        escribirExcelStreaming(filas, outputStream);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportarParaCoordinador(
            Long idCoordinador, String busqueda, Long idRol, Boolean activo, OutputStream outputStream
    ) {
        Usuario coordinador = usuarioRepository.findByIdWithRolAndNodo(idCoordinador)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinador no encontrado con id: " + idCoordinador));
        if (coordinador.getNodo() == null) {
            throw new BadRequestException("El coordinador no tiene un nodo asignado");
        }

        String pattern = normalizarBusqueda(busqueda);
        List<UsuarioExportacionProjection> filas = usuarioRepository.findParaExportacion(
                pattern, idRol, ROLES_COORDINADOR, coordinador.getNodo().getIdNodo(), activo
        );
        escribirExcelStreaming(filas, outputStream);
    }

    private void escribirExcelStreaming(List<UsuarioExportacionProjection> filas, OutputStream outputStream) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Usuarios");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int c = 0; c < COLUMNAS.size(); c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(COLUMNAS.get(c));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(c, ANCHOS_COLUMNAS[c] * 256);
            }

            int rowIdx = 1;
            for (UsuarioExportacionProjection u : filas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(valorSeguro(u.getNombres()));
                row.createCell(1).setCellValue(valorSeguro(u.getApellidos()));
                row.createCell(2).setCellValue(valorSeguro(u.getDocumento()));
                row.createCell(3).setCellValue(valorSeguro(u.getCorreo()));
                row.createCell(4).setCellValue(valorSeguro(u.getTelefono()));
                row.createCell(5).setCellValue(valorSeguro(u.getRol()));
                row.createCell(6).setCellValue(valorSeguro(u.getNodo()));
                row.createCell(7).setCellValue(valorSeguro(u.getGrupoSalon()));
                row.createCell(8).setCellValue(Boolean.TRUE.equals(u.getActivo()) ? "Activo" : "Inactivo");
            }

            workbook.write(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el archivo Excel de exportación", ex);
        }
    }

    private String normalizarBusqueda(String busqueda) {
        return (busqueda == null || busqueda.isBlank()) ? null : "%" + busqueda.trim().toLowerCase() + "%";
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }
}
