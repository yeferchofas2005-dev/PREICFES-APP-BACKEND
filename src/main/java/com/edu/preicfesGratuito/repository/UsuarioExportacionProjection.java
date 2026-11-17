package com.edu.preicfesGratuito.repository;

/**
 * Proyeccion ligera para exportar usuarios a Excel sin cargar entidades JPA completas.
 */
public interface UsuarioExportacionProjection {
    String getNombres();
    String getApellidos();
    String getDocumento();
    String getCorreo();
    String getTelefono();
    String getRol();
    String getNodo();
    String getGrupoSalon();
    Boolean getActivo();
}
