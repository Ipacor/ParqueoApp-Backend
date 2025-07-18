package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.SancionDetalleDto;
import com.parqueo.parkingApp.model.SancionDetalle;
import org.springframework.stereotype.Component;

@Component
public class SancionDetalleMapper {

    public static SancionDetalleDto toDto(SancionDetalle sancionDetalle) {
        if (sancionDetalle == null) {
            return null;
        }

        return SancionDetalleDto.builder()
                .id(sancionDetalle.getId())
                .descripcion(sancionDetalle.getDescripcion())
                .fechaSancion(sancionDetalle.getFechaSancion())
                .sancionId(sancionDetalle.getSancion() != null ? sancionDetalle.getSancion().getId() : null)
                .reglaId(sancionDetalle.getRegla() != null ? sancionDetalle.getRegla().getId() : null)
                .estado(sancionDetalle.getEstado() != null ? sancionDetalle.getEstado().name() : null)
                .build();
    }

    public static SancionDetalle toEntity(SancionDetalleDto sancionDetalleDto) {
        if (sancionDetalleDto == null) {
            return null;
        }

        return SancionDetalle.builder()
                .id(sancionDetalleDto.getId())
                .descripcion(sancionDetalleDto.getDescripcion())
                .fechaSancion(sancionDetalleDto.getFechaSancion())
                .estado(sancionDetalleDto.getEstado() != null ? 
                    SancionDetalle.EstadoDetalle.valueOf(sancionDetalleDto.getEstado()) : null)
                .build();
    }
}