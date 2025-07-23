package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.model.Notificacion;
import com.parqueo.parkingApp.model.Usuario;

import java.util.List;
import java.util.Map;

public interface NotificacionService {
    
    Notificacion crearNotificacion(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo);
    
    List<Notificacion> obtenerNotificacionesUsuario(Long usuarioId);
    
    List<Notificacion> obtenerNotificacionesNoLeidas(Long usuarioId);
    
    Long contarNotificacionesNoLeidas(Long usuarioId);
    
    void marcarComoLeida(Long notificacionId);
    
    void marcarTodasComoLeidas(Long usuarioId);
    
    void eliminarNotificacion(Long notificacionId);
    
    void eliminarNotificacionesAntiguas(Long usuarioId, int dias);
    
    // Nuevos métodos para estadísticas
    Map<String, Long> obtenerEstadisticasPorTipo(Long usuarioId);
    
    Map<String, Object> obtenerEstadisticasGenerales(Long usuarioId);
    
    // Métodos para recordatorios
    void crearRecordatorioEntrada(Usuario usuario, String espacio, String fecha);
    
    void crearRecordatorioSalida(Usuario usuario, String espacio, String fecha);
    
    void crearNotificacionMantenimiento(Usuario usuario, String mensaje);
    
    // Métodos para gestión avanzada
    List<Notificacion> obtenerNotificacionesPorTipo(Long usuarioId, Notificacion.TipoNotificacion tipo);
    
    void eliminarNotificacionesPorTipo(Long usuarioId, Notificacion.TipoNotificacion tipo);
} 