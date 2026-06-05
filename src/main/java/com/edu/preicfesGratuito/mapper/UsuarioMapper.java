package com.edu.preicfesGratuito.mapper;

import com.edu.preicfesGratuito.dto.RolDTO;
import com.edu.preicfesGratuito.dto.UsuarioResponseDTO;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

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
