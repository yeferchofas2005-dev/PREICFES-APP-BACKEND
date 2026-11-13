package com.edu.preicfesGratuito.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CasoExamenDTO {
    private Long idCaso;
    private String titulo;
    private String textoBase;
    private String rutaImagen;
}
