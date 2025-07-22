package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Notificacion;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.repository.NotificacionRepository;
import com.parqueo.parkingApp.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Notificacion crearNotificacion(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo)
                .leida(false)
                .fechaCreacion(LocalDateTime.now())
                .build();
        
        return notificacionRepository.save(notificacion);
    }

    @Override
    public List<Notificacion> obtenerNotificacionesUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @Override
    public List<Notificacion> obtenerNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaOrderByFechaCreacionDesc(usuarioId, false);
    }

    @Override
    public Long contarNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeida(usuarioId, false);
    }

    @Override
    public void marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada"));
        
        notificacion.marcarComoLeida();
        notificacionRepository.save(notificacion);
    }

    @Override
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndLeidaOrderByFechaCreacionDesc(usuarioId, false);
        for (Notificacion notificacion : notificaciones) {
            notificacion.marcarComoLeida();
        }
        notificacionRepository.saveAll(notificaciones);
    }

    @Override
    public void eliminarNotificacion(Long notificacionId) {
        if (!notificacionRepository.existsById(notificacionId)) {
            throw new EntityNotFoundException("Notificación no encontrada");
        }
        notificacionRepository.deleteById(notificacionId);
    }

    @Override
    public void eliminarNotificacionesAntiguas(Long usuarioId, int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        List<Notificacion> notificacionesAntiguas = notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .filter(n -> n.getFechaCreacion().isBefore(fechaLimite))
                .toList();
        
        notificacionRepository.deleteAll(notificacionesAntiguas);
    }
} 