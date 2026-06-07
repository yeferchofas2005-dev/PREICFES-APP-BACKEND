package com.edu.preicfesGratuito.security;

import com.edu.preicfesGratuito.entity.Usuario;
import com.edu.preicfesGratuito.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que carga usuarios para el proceso de autenticacion de Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Busca el usuario por correo y lo adapta a {@link UserDetails}.
     *
     * @param correo correo enviado como username en el login o extraido del JWT
     * @return principal de seguridad de la aplicacion
     * @throws UsernameNotFoundException cuando no existe un usuario con ese correo
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        return new UserDetailsImpl(usuario);
    }
}
