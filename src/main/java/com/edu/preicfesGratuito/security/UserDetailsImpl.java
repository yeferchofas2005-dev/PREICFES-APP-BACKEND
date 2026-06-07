package com.edu.preicfesGratuito.security;

import com.edu.preicfesGratuito.entity.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador entre la entidad {@link Usuario} y el contrato {@link UserDetails}.
 *
 * <p>Spring Security usa esta clase como principal autenticado dentro del
 * contexto de seguridad y como fuente de autoridades basadas en roles.</p>
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long idUsuario;
    private final String correo;
    private final String password;
    private final Boolean activo;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Crea el principal de seguridad a partir de un usuario persistido.
     *
     * @param usuario entidad de usuario cargada desde la base de datos
     */
    public UserDetailsImpl(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.correo = usuario.getCorreo();
        this.password = usuario.getPasswordHash();
        this.activo = usuario.getActivo();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
    }

    /**
     * Retorna el correo porque la aplicacion autentica usuarios por correo.
     */
    @Override
    public String getUsername() {
        return correo;
    }

    /**
     * Determina si la cuenta puede autenticarse segun el campo {@code activo}.
     */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(activo);
    }

    /**
     * La aplicacion no maneja expiracion de cuentas por ahora.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * La aplicacion no maneja bloqueo de cuentas por ahora.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * La aplicacion no maneja expiracion de credenciales por ahora.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
