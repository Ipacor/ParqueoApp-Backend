package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.ReglasEstacionamientoDto;
import com.parqueo.parkingApp.model.ReglasEstacionamiento;
import org.springframework.stereotype.Component;

@Component
public class ReglasEstacionamientoMapper {

    public static ReglasEstacionamientoDto toDto(ReglasEstacionamiento reglasEstacionamiento) {
        if (reglasEstacionamiento == null) {
            return null;
        }

        return ReglasEstacionamientoDto.builder()
                .id(reglasEstacionamiento.getId())
                .descripcion(reglasEstacionamiento.getDescripcion())
                .tipoFalta(reglasEstacionamiento.getTipoFalta())
                .build();
    }

    public static ReglasEstacionamiento toEntity(ReglasEstacionamientoDto reglasEstacionamientoDto) {
        if (reglasEstacionamientoDto == null) {
            return null;
        }

        return ReglasEstacionamiento.builder()
                .id(reglasEstacionamientoDto.getId())
                .descripcion(reglasEstacionamientoDto.getDescripcion())
                .tipoFalta(reglasEstacionamientoDto.getTipoFalta())
                .build();
    }
}