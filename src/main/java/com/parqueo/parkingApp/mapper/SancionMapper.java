package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.SancionDto;
import com.parqueo.parkingApp.model.Sancion;
import org.springframework.stereotype.Component;

@Component
public class SancionMapper {

    public static SancionDto toDto(Sancion sancion) {
        if (sancion == null) {
            return null;
        }

        Long reglaId = null;
        if (sancion.getDetalles() != null && !sancion.getDetalles().isEmpty()) {
            reglaId = sancion.getDetalles().iterator().next().getRegla().getId();
        }

        return SancionDto.builder()
                .id(sancion.getId())
                .usuarioId(sancion.getUsuario() != null ? sancion.getUsuario().getId() : null)
                .vehiculoId(sancion.getVehiculo() != null ? sancion.getVehiculo().getId() : null)
                .motivo(sancion.getMotivo())
                .estado(sancion.getEstado())
                .registroSancion(sancion.getRegistroSancion())
                .observaciones(sancion.getObservaciones())
                .fechaResolucion(sancion.getFechaResolucion())
                .registradaPorNombre(sancion.getRegistradaPor() != null ? sancion.getRegistradaPor().getNombreCompleto() : null)
                .tipoCastigo(sancion.getTipoCastigo())
                .fechaInicioSuspension(sancion.getFechaInicioSuspension())
                .fechaFinSuspension(sancion.getFechaFinSuspension())
                .reglaId(reglaId)
                .build();
    }

    public static Sancion toEntity(SancionDto sancionDto) {
        if (sancionDto == null) {
            return null;
        }

        return Sancion.builder()
                .id(sancionDto.getId())
                .motivo(sancionDto.getMotivo())
                .estado(sancionDto.getEstado())
                .registroSancion(sancionDto.getRegistroSancion())
                .observaciones(sancionDto.getObservaciones())
                .fechaResolucion(sancionDto.getFechaResolucion())
                .tipoCastigo(sancionDto.getTipoCastigo())
                .fechaInicioSuspension(sancionDto.getFechaInicioSuspension())
                .fechaFinSuspension(sancionDto.getFechaFinSuspension())
                .build();
    }
}