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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

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

    @Override
    public Map<String, Long> obtenerEstadisticasPorTipo(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return notificaciones.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    n -> n.getTipo().name(),
                    java.util.stream.Collectors.counting()
                ));
    }

    @Override
    public Map<String, Object> obtenerEstadisticasGenerales(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        long total = notificaciones.size();
        long noLeidas = notificaciones.stream().filter(n -> !n.getLeida()).count();
        long leidas = total - noLeidas;
        
        Map<String, Object> estadisticas = new java.util.HashMap<>();
        estadisticas.put("total", total);
        estadisticas.put("noLeidas", noLeidas);
        estadisticas.put("leidas", leidas);
        estadisticas.put("porTipo", obtenerEstadisticasPorTipo(usuarioId));
        
        return estadisticas;
    }

    @Override
    public void crearRecordatorioEntrada(Usuario usuario, String espacio, String fecha) {
        crearNotificacion(
            usuario,
            "Recordatorio: Entrada Pendiente",
            "Recuerda que tienes una reserva activa para el espacio " + espacio + 
            " el " + fecha + ". No olvides registrar tu entrada.",
            com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.RECORDATORIO_ENTRADA
        );
    }

    @Override
    public void crearRecordatorioSalida(Usuario usuario, String espacio, String fecha) {
        crearNotificacion(
            usuario,
            "Recordatorio: Salida Pendiente",
            "Tu reserva en el espacio " + espacio + " expira el " + fecha + 
            ". Recuerda registrar tu salida antes de que expire.",
            com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.RECORDATORIO_SALIDA
        );
    }

    @Override
    public void crearNotificacionMantenimiento(Usuario usuario, String mensaje) {
        crearNotificacion(
            usuario,
            "Mantenimiento del Sistema",
            mensaje,
            com.parqueo.parkingApp.model.Notificacion.TipoNotificacion.MANTENIMIENTO
        );
    }

    @Override
    public List<Notificacion> obtenerNotificacionesPorTipo(Long usuarioId, Notificacion.TipoNotificacion tipo) {
        return notificacionRepository.findByUsuarioIdAndTipoOrderByFechaCreacionDesc(usuarioId, tipo);
    }

    @Override
    public void eliminarNotificacionesPorTipo(Long usuarioId, Notificacion.TipoNotificacion tipo) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndTipoOrderByFechaCreacionDesc(usuarioId, tipo);
        notificacionRepository.deleteAll(notificaciones);
    }
} 