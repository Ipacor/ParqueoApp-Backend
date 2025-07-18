package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.VehiculoDto;
import com.parqueo.parkingApp.model.Vehiculo;
import org.springframework.stereotype.Component;

@Component
public class VehiculoMapper {

    public static VehiculoDto toDto(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }

        return VehiculoDto.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .color(vehiculo.getColor())
                .tipo(vehiculo.getTipo())
                .fechaRegistro(vehiculo.getFechaRegistro())
                .usuarioId(vehiculo.getUsuario() != null ? vehiculo.getUsuario().getId() : null)
                .activo(vehiculo.getActivo())
                .build();
    }

    public static Vehiculo toEntity(VehiculoDto vehiculoDto) {
        if (vehiculoDto == null) {
            return null;
        }

        return Vehiculo.builder()
                .id(vehiculoDto.getId())
                .placa(vehiculoDto.getPlaca())
                .marca(vehiculoDto.getMarca())
                .modelo(vehiculoDto.getModelo())
                .color(vehiculoDto.getColor())
                .tipo(vehiculoDto.getTipo())
                .fechaRegistro(vehiculoDto.getFechaRegistro())
                .activo(vehiculoDto.getActivo())
                .build();
    }
}
