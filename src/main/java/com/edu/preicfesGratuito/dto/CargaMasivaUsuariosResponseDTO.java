package com.edu.preicfesGratuito.dto;

import lombok.*;

import java.util.List;

/**
 * Resumen del resultado de una carga masiva de estudiantes desde Excel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargaMasivaUsuariosResponseDTO {
    private int totalFilasProcesadas;
    private int exitosos;
    private int fallidos;
    private int vinculadosAGrupo;
    private int encoladosParaEnvio;
    private List<CargaMasivaErrorDTO> errores;
}
