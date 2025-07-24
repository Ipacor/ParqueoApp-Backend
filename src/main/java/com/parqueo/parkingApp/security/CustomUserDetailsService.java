package com.parqueo.parkingApp.security;

import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import com.parqueo.parkingApp.repository.SancionRepository;
import com.parqueo.parkingApp.model.Sancion;
import com.parqueo.parkingApp.repository.ReservaRepository;
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
    private final ReservaRepository reservaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Lógica de suspensión total mejorada
        List<Sancion> sanciones = sancionRepository.findByUsuarioId(usuario.getId());
        boolean suspendido = false;
        LocalDateTime fechaFinSuspension = null;
        String tipoSuspension = "";
        
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
                tipoSuspension = s.getTipoCastigo();
                break;
            }
        }
        
        if (suspendido) {
            // Permitir login solo si tiene reservas activas o expiradas
            boolean tieneReservasPendientes = reservaRepository.findByUsuarioId(usuario.getId()).stream()
                .anyMatch(r -> r.getEstado() == com.parqueo.parkingApp.model.Reserva.EstadoReserva.ACTIVO ||
                              r.getEstado() == com.parqueo.parkingApp.model.Reserva.EstadoReserva.EXPIRADO);
            if (!tieneReservasPendientes) {
                String mensaje = String.format("🚫 ACCESO DENEGADO: Tu cuenta está suspendida por '%s'. " +
                        "No puedes acceder al sistema hasta que finalice la suspensión.", tipoSuspension);
                if (fechaFinSuspension != null) {
                    long diasRestantes = java.time.Duration.between(LocalDateTime.now(), fechaFinSuspension).toDays();
                    long horasRestantes = java.time.Duration.between(LocalDateTime.now(), fechaFinSuspension).toHours() % 24;
                    if (diasRestantes > 0) {
                        mensaje += String.format(" Tiempo restante: %d días y %d horas.", diasRestantes, horasRestantes);
                    } else {
                        mensaje += String.format(" Tiempo restante: %d horas.", horasRestantes);
                    }
                }
                throw new LockedException(mensaje);
            }
            // Si tiene reservas activas/expiradas, permitir login pero limitar permisos en el frontend
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