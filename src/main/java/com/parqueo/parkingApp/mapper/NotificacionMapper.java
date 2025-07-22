package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.NotificacionDto;
import com.parqueo.parkingApp.model.Notificacion;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {
    public static NotificacionDto toDto(Notificacion notificacion) {
        if (notificacion == null) {
            return null;
        }
        NotificacionDto dto = new NotificacionDto();
        dto.setId(notificacion.getId());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setTipo(notificacion.getTipo() != null ? notificacion.getTipo().name() : null);
        dto.setLeida(notificacion.getLeida());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        return dto;
    }
} 