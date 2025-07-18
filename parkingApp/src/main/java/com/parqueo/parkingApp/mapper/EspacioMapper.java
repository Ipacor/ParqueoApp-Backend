package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.EspacioDisponibleDto;
import com.parqueo.parkingApp.model.EspacioDisponible;
import org.springframework.stereotype.Component;

@Component
public class EspacioMapper {

    public static EspacioDisponibleDto toDto(EspacioDisponible espacio) {
        if (espacio == null) {
            return null;
        }

        return EspacioDisponibleDto.builder()
                .id(espacio.getId())
                .ubicacion(espacio.getUbicacion())
                .numeroEspacio(espacio.getNumeroEspacio())
                .zona(espacio.getZona())
                .estado(espacio.getEstado())
                .capacidadMaxima(espacio.getCapacidadMaxima())
                .activo(espacio.getActivo())
                .fechaRegistro(espacio.getFechaRegistro())
                .build();
    }

    public static EspacioDisponible toEntity(EspacioDisponibleDto espacioDto) {
        if (espacioDto == null) {
            return null;
        }

        return EspacioDisponible.builder()
                .id(espacioDto.getId())
                .ubicacion(espacioDto.getUbicacion())
                .numeroEspacio(espacioDto.getNumeroEspacio())
                .zona(espacioDto.getZona())
                .estado(espacioDto.getEstado())
                .capacidadMaxima(espacioDto.getCapacidadMaxima())
                .activo(espacioDto.getActivo())
                .fechaRegistro(espacioDto.getFechaRegistro())
                .build();
    }
}
