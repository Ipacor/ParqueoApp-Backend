package com.parqueo.parkingApp.security;

import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.model.Sancion;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.LockedException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final SancionRepository sancionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Lógica de suspensión total
        List<Sancion> sanciones = sancionRepository.findByUsuarioId(usuario.getId());
        boolean suspendido = false;
        LocalDateTime fechaFinSuspension = null;
        for (Sancion s : sanciones) {
            if (s.getTipoCastigo() != null &&
                s.getTipoCastigo().toLowerCase().contains("suspensión") &&
                s.getEstado() == Sancion.EstadoSancion.ACTIVA &&
                s.getFechaInicioSuspension() != null &&
                s.getFechaFinSuspension() != null &&
                LocalDateTime.now().isAfter(s.getFechaInicioSuspension()) &&
                LocalDateTime.now().isBefore(s.getFechaFinSuspension())) {
                suspendido = true;
                fechaFinSuspension = s.getFechaFinSuspension();
                break;
            }
        }
        if (suspendido) {
            String mensaje = "Usuario suspendido. No puede acceder hasta que finalice la suspensión.";
            if (fechaFinSuspension != null) {
                mensaje += "|" + fechaFinSuspension.toString();
            }
            throw new LockedException(mensaje);
        }

        // Crear autoridades basadas en el rol y sus permisos
        List<SimpleGrantedAuthority> authorities = usuario.getRol().getPermisos().stream()
                .map(permiso -> new SimpleGrantedAuthority(permiso.getNombre()))
                .collect(Collectors.toList());
        
        // Agregar también el rol como autoridad
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .disabled(!usuario.getActivo())
                .build();
    }
} 