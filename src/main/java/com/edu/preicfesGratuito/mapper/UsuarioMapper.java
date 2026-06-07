package com.edu.preicfesGratuito.mapper;

import com.edu.preicfesGratuito.dto.RolDTO;
import com.edu.preicfesGratuito.dto.UsuarioResponseDTO;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import org.springframework.stereotype.Component;

/**
 * Mapper manual para exponer datos publicos de usuario.
 *
 * <p>Evita devolver entidades JPA directamente y omite campos sensibles como
 * el hash de la contrasena.</p>
 */
@Component
public class UsuarioMapper {

    /**
     * Convierte una entidad {@link Usuario} a su DTO de respuesta.
     *
     * @param usuario entidad persistida
     * @return DTO seguro para exponer por API
     */
    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .documento(usuario.getDocumento())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .activo(usuario.getActivo())
                .rol(toRolDTO(usuario.getRol()))
                .build();
    }

    /**
     * Convierte el rol asociado al usuario a un DTO simple.
     */
    private RolDTO toRolDTO(Rol rol) {
        if (rol == null) {
            return null;
        }

        return RolDTO.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .build();
    }
}
