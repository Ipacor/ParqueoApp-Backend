package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Notificacion;
import com.parqueo.parkingApp.model.Usuario;

import java.util.List;

public interface NotificacionService {
    
    Notificacion crearNotificacion(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo);
    
    List<Notificacion> obtenerNotificacionesUsuario(Long usuarioId);
    
    List<Notificacion> obtenerNotificacionesNoLeidas(Long usuarioId);
    
    Long contarNotificacionesNoLeidas(Long usuarioId);
    
    void marcarComoLeida(Long notificacionId);
    
    void marcarTodasComoLeidas(Long usuarioId);
    
    void eliminarNotificacion(Long notificacionId);
    
    void eliminarNotificacionesAntiguas(Long usuarioId, int dias);
} 