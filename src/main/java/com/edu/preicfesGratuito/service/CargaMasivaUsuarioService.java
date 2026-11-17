package com.edu.preicfesGratuito.service;

import com.edu.preicfesGratuito.dto.CargaMasivaUsuariosResponseDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para descarga de plantilla y carga masiva de estudiantes desde Excel (.xlsx).
 */
public interface CargaMasivaUsuarioService {

    /**
     * Genera una plantilla .xlsx con encabezados estandar y fila de ejemplo.
     */
    byte[] generarPlantillaExcel();

    /**
     * Procesa un archivo .xlsx para registrar y vincular estudiantes en lote.
     */
    CargaMasivaUsuariosResponseDTO cargarEstudiantesDesdeExcel(
            Long idActor,
            Long idNodoDestino,
            MultipartFile archivo,
            boolean esAdmin
    );
}
