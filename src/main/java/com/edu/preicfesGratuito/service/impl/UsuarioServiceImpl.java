package com.edu.preicfesGratuito.service.impl;

import com.edu.preicfesGratuito.dto.*;
import com.edu.preicfesGratuito.entity.Nodo;
import com.edu.preicfesGratuito.entity.Rol;
import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.enums.RolNombre;
import com.edu.preicfesGratuito.exception.BadRequestException;
import com.edu.preicfesGratuito.exception.ResourceNotFoundException;
import com.edu.preicfesGratuito.mapper.UsuarioMapper;
import com.edu.preicfesGratuito.repository.NodoRepository;
import com.edu.preicfesGratuito.repository.RolRepository;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import com.edu.preicfesGratuito.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Implementacion de la gestion de usuarios con aislamiento por rol y nodo.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final Set<String> ROLES_COORDINADOR = Set.of(
            RolNombre.PROFESOR.name(),
            RolNombre.ESTUDIANTE.name()
    );
    private static final List<String> ROLES_COORDINADOR_LIST = List.of(
            RolNombre.PROFESOR.name(),
            RolNombre.ESTUDIANTE.name()
    );

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final NodoRepository nodoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<UsuarioResponseDTO> listarParaAdmin(
            String busqueda, Long idRol, Long idNodo, Boolean activo, int page, int size
    ) {
        return ejecutarListado(busqueda, idRol, null, idNodo, activo, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerParaAdmin(Long idUsuario) {
        return usuarioMapper.toResponseDTO(buscarUsuarioPorId(idUsuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crearParaAdmin(UsuarioCrearDTO request) {
        Rol rol = buscarRolPorId(request.getIdRol());
        Nodo nodo = resolverNodoParaRol(rol, request.getIdNodo());
        return crearUsuarioInterno(request, rol, nodo);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarParaAdmin(Long idUsuario, UsuarioActualizarDTO request) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        Rol rol = buscarRolPorId(request.getIdRol());
        Nodo nodo = resolverNodoParaRol(rol, request.getIdNodo());
        return actualizarUsuarioInterno(usuario, request, rol, nodo);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstadoParaAdmin(Long idActor, Long idUsuario, CambiarEstadoDTO request) {
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        return cambiarEstadoInterno(idActor, usuario, request.getActivo());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioCatalogosDTO obtenerCatalogosAdmin() {
        List<RolDTO> roles = rolRepository.findAll(Sort.by(Sort.Direction.ASC, "idRol")).stream()
                .map(usuarioMapper::toRolDTO)
                .toList();
        List<NodoSimpleDTO> nodos = nodoRepository.findAllActivosWithLocalidad().stream()
                .map(usuarioMapper::toNodoSimpleDTO)
                .toList();
        return UsuarioCatalogosDTO.builder().roles(roles).nodos(nodos).build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<UsuarioResponseDTO> listarParaCoordinador(
            Long idCoordinador, String busqueda, Long idRol, Boolean activo, int page, int size
    ) {
        Nodo nodoCoordinador = obtenerNodoCoordinador(idCoordinador);
        return ejecutarListado(busqueda, idRol, ROLES_COORDINADOR_LIST, nodoCoordinador.getIdNodo(), activo, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerParaCoordinador(Long idCoordinador, Long idUsuario) {
        Nodo nodoCoordinador = obtenerNodoCoordinador(idCoordinador);
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        validarAlcanceCoordinador(usuario, nodoCoordinador);
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crearParaCoordinador(Long idCoordinador, UsuarioCrearDTO request) {
        Nodo nodoCoordinador = obtenerNodoCoordinador(idCoordinador);
        Rol rol = buscarRolPorId(request.getIdRol());
        validarRolPermitidoCoordinador(rol);
        return crearUsuarioInterno(request, rol, nodoCoordinador);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarParaCoordinador(Long idCoordinador, Long idUsuario, UsuarioActualizarDTO request) {
        Nodo nodoCoordinador = obtenerNodoCoordinador(idCoordinador);
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        validarAlcanceCoordinador(usuario, nodoCoordinador);

        Rol rolDestino = buscarRolPorId(request.getIdRol());
        validarRolPermitidoCoordinador(rolDestino);

        return actualizarUsuarioInterno(usuario, request, rolDestino, nodoCoordinador);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarEstadoParaCoordinador(Long idCoordinador, Long idUsuario, CambiarEstadoDTO request) {
        Nodo nodoCoordinador = obtenerNodoCoordinador(idCoordinador);
        Usuario usuario = buscarUsuarioPorId(idUsuario);
        validarAlcanceCoordinador(usuario, nodoCoordinador);
        return cambiarEstadoInterno(idCoordinador, usuario, request.getActivo());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioCatalogosDTO obtenerCatalogosCoordinador(Long idCoordinador) {
        Nodo nodoCoordinador = obtenerNodoCoordinador(idCoordinador);
        List<RolDTO> roles = rolRepository.findAll(Sort.by(Sort.Direction.ASC, "idRol")).stream()
                .filter(rol -> ROLES_COORDINADOR.contains(rol.getNombre()))
                .map(usuarioMapper::toRolDTO)
                .toList();
        return UsuarioCatalogosDTO.builder()
                .roles(roles)
                .nodos(List.of(usuarioMapper.toNodoSimpleDTO(nodoCoordinador)))
                .build();
    }

    private PaginaResponseDTO<UsuarioResponseDTO> ejecutarListado(
            String busqueda, Long idRol, List<String> rolesPermitidos, Long idNodo, Boolean activo, int page, int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "idUsuario"));
        String pattern = (busqueda == null || busqueda.isBlank()) ? null : "%" + busqueda.trim().toLowerCase() + "%";

        Page<Usuario> resultado = usuarioRepository.buscarConFiltros(
                pattern, idRol, rolesPermitidos, idNodo, activo, pageable
        );

        return PaginaResponseDTO.<UsuarioResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(usuarioMapper::toResponseDTO).toList())
                .paginaActual(resultado.getNumber())
                .tamanoPagina(resultado.getSize())
                .totalElementos(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .ultima(resultado.isLast())
                .build();
    }

    private UsuarioResponseDTO crearUsuarioInterno(UsuarioCrearDTO request, Rol rol, Nodo nodo) {
        String documento = request.getDocumento().trim();
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByDocumento(documento)) {
            throw new BadRequestException("Ya existe un usuario con ese documento");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new BadRequestException("Ya existe un usuario con ese correo");
        }

        Usuario nuevo = Usuario.builder()
                .rol(rol)
                .nodo(nodo)
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .documento(documento)
                .correo(correo)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(normalizarTextoOpcional(request.getTelefono()))
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return usuarioMapper.toResponseDTO(usuarioRepository.save(nuevo));
    }

    private UsuarioResponseDTO actualizarUsuarioInterno(
            Usuario usuario, UsuarioActualizarDTO request, Rol rol, Nodo nodo
    ) {
        String documento = request.getDocumento().trim();
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByDocumentoAndIdUsuarioNot(documento, usuario.getIdUsuario())) {
            throw new BadRequestException("Ya existe otro usuario con ese documento");
        }
        if (usuarioRepository.existsByCorreoAndIdUsuarioNot(correo, usuario.getIdUsuario())) {
            throw new BadRequestException("Ya existe otro usuario con ese correo");
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        usuario.setRol(rol);
        usuario.setNodo(nodo);
        usuario.setNombres(request.getNombres().trim());
        usuario.setApellidos(request.getApellidos().trim());
        usuario.setDocumento(documento);
        usuario.setCorreo(correo);
        usuario.setTelefono(normalizarTextoOpcional(request.getTelefono()));
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    private UsuarioResponseDTO cambiarEstadoInterno(Long idActor, Usuario usuario, Boolean activo) {
        if (usuario.getIdUsuario().equals(idActor) && Boolean.FALSE.equals(activo)) {
            throw new BadRequestException("No puedes inactivar tu propio usuario");
        }
        usuario.setActivo(activo);
        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuarioPorId(Long idUsuario) {
        return usuarioRepository.findByIdWithRolAndNodo(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + idUsuario));
    }

    private Rol buscarRolPorId(Long idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + idRol));
    }

    private Nodo resolverNodoParaRol(Rol rol, Long idNodo) {
        boolean requiereNodo = !RolNombre.ADMIN.name().equals(rol.getNombre());
        if (idNodo == null) {
            if (requiereNodo) {
                throw new BadRequestException("Los usuarios con rol " + rol.getNombre() + " deben tener un nodo asignado");
            }
            return null;
        }
        return nodoRepository.findById(idNodo)
                .orElseThrow(() -> new ResourceNotFoundException("Nodo no encontrado con id: " + idNodo));
    }

    private Nodo obtenerNodoCoordinador(Long idCoordinador) {
        Usuario coordinador = buscarUsuarioPorId(idCoordinador);
        if (coordinador.getNodo() == null) {
            throw new BadRequestException("El coordinador no tiene un nodo asignado");
        }
        return coordinador.getNodo();
    }

    private void validarRolPermitidoCoordinador(Rol rol) {
        if (!ROLES_COORDINADOR.contains(rol.getNombre())) {
            throw new AccessDeniedException("Un coordinador solo puede gestionar usuarios con rol PROFESOR o ESTUDIANTE");
        }
    }

    private void validarAlcanceCoordinador(Usuario usuario, Nodo nodoCoordinador) {
        if (!ROLES_COORDINADOR.contains(usuario.getRol().getNombre())) {
            throw new AccessDeniedException("No tienes permiso para gestionar usuarios con rol " + usuario.getRol().getNombre());
        }
        if (usuario.getNodo() == null || !usuario.getNodo().getIdNodo().equals(nodoCoordinador.getIdNodo())) {
            throw new AccessDeniedException("No puedes gestionar usuarios que pertenecen a otro nodo");
        }
    }

    private String normalizarTextoOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
