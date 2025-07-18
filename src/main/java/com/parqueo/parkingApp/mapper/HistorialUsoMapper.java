package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.HistorialUsoDto;
import com.parqueo.parkingApp.model.HistorialUso;
import org.springframework.stereotype.Component;

@Component
public class HistorialUsoMapper {

    public static HistorialUsoDto toDto(HistorialUso historialUso) {
        if (historialUso == null) {
            return null;
        }

        return HistorialUsoDto.builder()
                .id(historialUso.getId())
                .fechaUso(historialUso.getFechaUso())
                .notas(historialUso.getNotas())
                .usuarioId(historialUso.getUsuario() != null ? historialUso.getUsuario().getId() : null)
                .usuarioNombre(historialUso.getUsuario() != null ? historialUso.getUsuario().getNombreCompleto() : null)
                .vehiculoPlaca(historialUso.getVehiculo() != null ? historialUso.getVehiculo().getPlaca() : null)
                .espacioId(historialUso.getEspacio() != null ? historialUso.getEspacio().getId() : null)
                .accion(historialUso.getAccion() != null ? historialUso.getAccion().name() : null)
                .build();
    }

    public static HistorialUso toEntity(HistorialUsoDto historialUsoDto) {
        if (historialUsoDto == null) {
            return null;
        }

        return HistorialUso.builder()
                .id(historialUsoDto.getId())
                .fechaUso(historialUsoDto.getFechaUso())
                .notas(historialUsoDto.getNotas())
                .accion(historialUsoDto.getAccion() != null ? 
                    HistorialUso.AccionHistorial.valueOf(historialUsoDto.getAccion()) : null)
                .build();
    }
}
