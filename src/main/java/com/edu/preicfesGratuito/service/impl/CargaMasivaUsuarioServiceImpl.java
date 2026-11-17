package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.CargaMasivaErrorDTO;
import com.edu.preicfesGratuito.dto.CargaMasivaUsuariosResponseDTO;
import com.edu.preicfesGratuito.entity.*;
import com.edu.preicfesGratuito.enums.RolNombre;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.repository.*;
import com.edu.preicfesGratuito.service.CargaMasivaUsuarioService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementacion eficiente de carga masiva de estudiantes desde archivos .xlsx.
 *
 * <p>Valida duplicados internos y contra BD en lotes para evitar consultas N+1
 * y reporta errores individuales por fila sin abortar el resto del lote.</p>
 */
@Service
@RequiredArgsConstructor
public class CargaMasivaUsuarioServiceImpl implements CargaMasivaUsuarioService {

    private static final int BATCH_SIZE = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final List<String> COLUMNAS_PLANTILLA = List.of(
            "nombres", "apellidos", "documento", "correo", "telefono", "grupo_salon", "password"
    );

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final NodoRepository nodoRepository;
    private final GrupoRepository grupoRepository;
    private final GrupoEstudianteRepository grupoEstudianteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public byte[] generarPlantillaExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Estudiantes");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUMNAS_PLANTILLA.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(COLUMNAS_PLANTILLA.get(i));
                cell.setCellStyle(headerStyle);
            }

            Row ejemplo = sheet.createRow(1);
            ejemplo.createCell(0).setCellValue("Laura");
            ejemplo.createCell(1).setCellValue("Martinez");
            ejemplo.createCell(2).setCellValue("1099000001");
            ejemplo.createCell(3).setCellValue("laura.martinez@preicfes.com");
            ejemplo.createCell(4).setCellValue("3001234567");
            ejemplo.createCell(5).setCellValue("Kennedy-1001");
            ejemplo.createCell(6).setCellValue("");

            for (int i = 0; i < COLUMNAS_PLANTILLA.size(); i++) {
                sheet.setColumnWidth(i, 24 * 256);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la plantilla Excel", ex);
        }
    }

    @Override
    @Transactional
    public CargaMasivaUsuariosResponseDTO cargarEstudiantesDesdeExcel(
            Long idActor, Long idNodoDestino, MultipartFile archivo, boolean esAdmin
    ) {
        validarArchivoExcel(archivo);

        Nodo nodoDestino = resolverNodoDestino(idActor, idNodoDestino, esAdmin);
        Rol rolEstudiante = rolRepository.findByNombre(RolNombre.ESTUDIANTE.name())
                .orElseThrow(() -> new ResourceNotFoundException("Rol ESTUDIANTE no encontrado"));

        Map<String, Grupo> gruposPorNombre = cargarMapaGrupos(nodoDestino.getIdNodo());
        List<FilaLeida> filasLeidas = leerFilasExcel(archivo);

        if (filasLeidas.isEmpty()) {
            throw new BadRequestException("El archivo Excel no contiene filas de estudiantes para procesar");
        }

        List<CargaMasivaErrorDTO> errores = new ArrayList<>();
        List<FilaCandidata> candidatas = validarSintaxisYDuplicadosInternos(filasLeidas, gruposPorNombre, errores);

        Set<String> documentosExistentes = consultarDocumentosExistentes(candidatas);
        Set<String> correosExistentes = consultarCorreosExistentes(candidatas);

        List<FilaCandidata> validasParaPersistir = new ArrayList<>();
        for (FilaCandidata candidata : candidatas) {
            if (documentosExistentes.contains(candidata.documento())) {
                errores.add(errorFila(candidata.numeroFila(), candidata.documento(), candidata.correo(),
                        "Ya existe un usuario registrado con ese documento"));
                continue;
            }
            if (correosExistentes.contains(candidata.correo())) {
                errores.add(errorFila(candidata.numeroFila(), candidata.documento(), candidata.correo(),
                        "Ya existe un usuario registrado con ese correo"));
                continue;
            }
            validasParaPersistir.add(candidata);
        }

        int exitosos = 0;
        int vinculadosAGrupo = 0;
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < validasParaPersistir.size(); i += BATCH_SIZE) {
            List<FilaCandidata> chunk = validasParaPersistir.subList(i, Math.min(i + BATCH_SIZE, validasParaPersistir.size()));
            List<Usuario> usuariosChunk = chunk.stream()
                    .map(c -> Usuario.builder()
                            .rol(rolEstudiante)
                            .nodo(nodoDestino)
                            .nombres(c.nombres())
                            .apellidos(c.apellidos())
                            .documento(c.documento())
                            .correo(c.correo())
                            .passwordHash(passwordEncoder.encode(c.passwordPlano()))
                            .telefono(c.telefono())
                            .activo(true)
                            .build())
                    .toList();

            List<Usuario> guardados = usuarioRepository.saveAllAndFlush(usuariosChunk);
            exitosos += guardados.size();

            List<GrupoEstudiante> vinculaciones = new ArrayList<>();
            for (int idx = 0; idx < chunk.size(); idx++) {
                Grupo grupo = chunk.get(idx).grupo();
                if (grupo != null) {
                    Usuario estudiante = guardados.get(idx);
                    vinculaciones.add(GrupoEstudiante.builder()
                            .idEstudiante(estudiante.getIdUsuario())
                            .grupo(grupo)
                            .fechaIngreso(hoy)
                            .build());
                }
            }

            if (!vinculaciones.isEmpty()) {
                grupoEstudianteRepository.saveAllAndFlush(vinculaciones);
                vinculadosAGrupo += vinculaciones.size();
            }
        }

        errores.sort(Comparator.comparingInt(CargaMasivaErrorDTO::getFila));

        return CargaMasivaUsuariosResponseDTO.builder()
                .totalFilasProcesadas(filasLeidas.size())
                .exitosos(exitosos)
                .fallidos(errores.size())
                .vinculadosAGrupo(vinculadosAGrupo)
                .errores(errores)
                .build();
    }

    private void validarArchivoExcel(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Debes adjuntar un archivo Excel (.xlsx) no vacío");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".xlsx")) {
            throw new BadRequestException("Formato no soportado. El archivo debe tener extensión .xlsx");
        }
    }

    private Nodo resolverNodoDestino(Long idActor, Long idNodoDestino, boolean esAdmin) {
        if (esAdmin) {
            if (idNodoDestino == null) {
                throw new BadRequestException("El administrador debe seleccionar un nodo destino para la carga masiva");
            }
            return nodoRepository.findById(idNodoDestino)
                    .orElseThrow(() -> new ResourceNotFoundException("Nodo no encontrado con id: " + idNodoDestino));
        }

        Usuario coordinador = usuarioRepository.findByIdWithRolAndNodo(idActor)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinador no encontrado con id: " + idActor));
        if (coordinador.getNodo() == null) {
            throw new BadRequestException("El coordinador no tiene un nodo asignado");
        }
        return coordinador.getNodo();
    }

    private Map<String, Grupo> cargarMapaGrupos(Long idNodo) {
        Map<String, Grupo> mapa = new HashMap<>();
        for (Grupo grupo : grupoRepository.findByNodoIdNodoAndActivoTrue(idNodo)) {
            mapa.put(grupo.getNombre().trim().toLowerCase(), grupo);
        }
        return mapa;
    }

    private List<FilaLeida> leerFilasExcel(MultipartFile archivo) {
        DataFormatter formatter = new DataFormatter();
        List<FilaLeida> filas = new ArrayList<>();

        try (InputStream is = archivo.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new BadRequestException("El archivo Excel no contiene hojas");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BadRequestException("El archivo Excel no contiene fila de encabezados");
            }

            Map<String, Integer> indices = resolverIndicesColumnas(headerRow, formatter);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || esFilaVacia(row, formatter)) {
                    continue;
                }

                filas.add(new FilaLeida(
                        r + 1,
                        leerCelda(row, indices.get("nombres"), formatter),
                        leerCelda(row, indices.get("apellidos"), formatter),
                        leerCelda(row, indices.get("documento"), formatter),
                        leerCelda(row, indices.get("correo"), formatter),
                        leerCelda(row, indices.get("telefono"), formatter),
                        leerCelda(row, indices.get("grupo_salon"), formatter),
                        leerCelda(row, indices.get("password"), formatter)
                ));
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("No se pudo leer el archivo .xlsx. Verifica que no esté corrupto");
        }

        return filas;
    }

    private Map<String, Integer> resolverIndicesColumnas(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> encontrados = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String valor = leerCelda(headerRow, c, formatter).toLowerCase().replace(" ", "_");
            if (!valor.isEmpty()) {
                encontrados.put(valor, c);
            }
        }

        if (!encontrados.containsKey("nombres") || !encontrados.containsKey("apellidos")
                || !encontrados.containsKey("documento") || !encontrados.containsKey("correo")) {
            throw new BadRequestException(
                    "Encabezados inválidos. Columnas requeridas: nombres, apellidos, documento, correo (opcionales: telefono, grupo_salon, password)"
            );
        }

        Map<String, Integer> resultado = new HashMap<>();
        resultado.put("nombres", encontrados.get("nombres"));
        resultado.put("apellidos", encontrados.get("apellidos"));
        resultado.put("documento", encontrados.get("documento"));
        resultado.put("correo", encontrados.get("correo"));
        resultado.put("telefono", encontrados.get("telefono"));
        Integer grupoIdx = encontrados.containsKey("grupo_salon")
                ? encontrados.get("grupo_salon")
                : encontrados.get("grupo");
        resultado.put("grupo_salon", grupoIdx);
        resultado.put("password", encontrados.get("password"));
        return resultado;
    }

    private List<FilaCandidata> validarSintaxisYDuplicadosInternos(
            List<FilaLeida> filas, Map<String, Grupo> gruposPorNombre, List<CargaMasivaErrorDTO> errores
    ) {
        List<FilaCandidata> candidatas = new ArrayList<>();
        Set<String> docsArchivo = new HashSet<>();
        Set<String> correosArchivo = new HashSet<>();

        for (FilaLeida fila : filas) {
            String nombres = fila.nombres().trim();
            String apellidos = fila.apellidos().trim();
            String documento = fila.documento().trim();
            String correo = fila.correo().trim().toLowerCase();
            String telefono = fila.telefono().trim();
            String grupoNombre = fila.grupoSalon().trim();
            String password = fila.password().trim();

            if (nombres.isEmpty() || apellidos.isEmpty() || documento.isEmpty() || correo.isEmpty()) {
                errores.add(errorFila(fila.numeroFila(), documento, correo,
                        "Faltan campos obligatorios (nombres, apellidos, documento o correo)"));
                continue;
            }
            if (nombres.length() > 100 || apellidos.length() > 100 || documento.length() > 20 || correo.length() > 150) {
                errores.add(errorFila(fila.numeroFila(), documento, correo,
                        "Uno o más campos superan la longitud máxima permitida"));
                continue;
            }
            if (!EMAIL_PATTERN.matcher(correo).matches()) {
                errores.add(errorFila(fila.numeroFila(), documento, correo, "Formato de correo electrónico inválido"));
                continue;
            }
            if (telefono.length() > 20) {
                errores.add(errorFila(fila.numeroFila(), documento, correo, "El teléfono supera 20 caracteres"));
                continue;
            }
            if (!password.isEmpty() && (password.length() < 6 || password.length() > 100)) {
                errores.add(errorFila(fila.numeroFila(), documento, correo,
                        "La contraseña debe tener entre 6 y 100 caracteres"));
                continue;
            }

            Grupo grupoDestino = null;
            if (!grupoNombre.isEmpty()) {
                grupoDestino = gruposPorNombre.get(grupoNombre.toLowerCase());
                if (grupoDestino == null) {
                    errores.add(errorFila(fila.numeroFila(), documento, correo,
                            "El grupo/salón '" + grupoNombre + "' no existe o no está activo en el nodo"));
                    continue;
                }
            }

            if (!docsArchivo.add(documento)) {
                errores.add(errorFila(fila.numeroFila(), documento, correo, "Documento duplicado dentro del archivo"));
                continue;
            }
            if (!correosArchivo.add(correo)) {
                errores.add(errorFila(fila.numeroFila(), documento, correo, "Correo duplicado dentro del archivo"));
                continue;
            }

            candidatas.add(new FilaCandidata(
                    fila.numeroFila(),
                    nombres,
                    apellidos,
                    documento,
                    correo,
                    telefono.isEmpty() ? null : telefono,
                    grupoDestino,
                    password.isEmpty() ? documento : password
            ));
        }

        return candidatas;
    }

    private Set<String> consultarDocumentosExistentes(List<FilaCandidata> candidatas) {
        Set<String> existentes = new HashSet<>();
        List<String> todos = candidatas.stream().map(FilaCandidata::documento).toList();
        for (int i = 0; i < todos.size(); i += BATCH_SIZE) {
            existentes.addAll(usuarioRepository.findDocumentosExistentes(
                    todos.subList(i, Math.min(i + BATCH_SIZE, todos.size()))
            ));
        }
        return existentes;
    }

    private Set<String> consultarCorreosExistentes(List<FilaCandidata> candidatas) {
        Set<String> existentes = new HashSet<>();
        List<String> todos = candidatas.stream().map(FilaCandidata::correo).toList();
        for (int i = 0; i < todos.size(); i += BATCH_SIZE) {
            existentes.addAll(usuarioRepository.findCorreosExistentes(
                    todos.subList(i, Math.min(i + BATCH_SIZE, todos.size()))
            ));
        }
        return existentes;
    }

    private boolean esFilaVacia(Row row, DataFormatter formatter) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            if (!leerCelda(row, c, formatter).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String leerCelda(Row row, Integer idx, DataFormatter formatter) {
        if (idx == null || idx < 0) {
            return "";
        }
        Cell cell = row.getCell(idx);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private CargaMasivaErrorDTO errorFila(int fila, String documento, String correo, String motivo) {
        return CargaMasivaErrorDTO.builder()
                .fila(fila)
                .documento(documento)
                .correo(correo)
                .motivo(motivo)
                .build();
    }

    private record FilaLeida(
            int numeroFila,
            String nombres,
            String apellidos,
            String documento,
            String correo,
            String telefono,
            String grupoSalon,
            String password
    ) {}

    private record FilaCandidata(
            int numeroFila,
            String nombres,
            String apellidos,
            String documento,
            String correo,
            String telefono,
            Grupo grupo,
            String passwordPlano
    ) {}
}
