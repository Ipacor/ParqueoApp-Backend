package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.EscaneoQRDto;
import com.parqueo.parkingApp.model.EscaneoQR;
import org.springframework.stereotype.Component;

@Component
public class EscaneoQRMapper {

    public static EscaneoQRDto toDto(EscaneoQR escaneoQR) {
        if (escaneoQR == null) {
            return null;
        }

        EscaneoQRDto dto = EscaneoQRDto.builder()
                .id(escaneoQR.getId())
                .reservaId(escaneoQR.getReserva() != null ? escaneoQR.getReserva().getId() : null)
                .tipo(escaneoQR.getTipo())
                .fechaInicioValidez(escaneoQR.getFechaInicioValidez())
                .build();
        
        // Establecer el token después de la construcción
        dto.setToken(escaneoQR.getToken());
        
        return dto;
    }

    public static EscaneoQR toEntity(EscaneoQRDto escaneoQRDto) {
        if (escaneoQRDto == null) {
            return null;
        }

        return EscaneoQR.builder()
                .id(escaneoQRDto.getId())
                .tipo(escaneoQRDto.getTipo())
                .fechaInicioValidez(escaneoQRDto.getFechaInicioValidez())
                .token(escaneoQRDto.getToken())
                .build();
    }
}
