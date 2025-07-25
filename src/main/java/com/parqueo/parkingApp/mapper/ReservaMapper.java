package com.parqueo.parkingApp.mapper;

import com.parqueo.parkingApp.dto.ReservaDto;
import com.parqueo.parkingApp.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public static ReservaDto toDto(Reserva reserva) {
        if (reserva == null) {
            return null;
        }

        ReservaDto.ReservaDtoBuilder builder = ReservaDto.builder()
                .id(reserva.getId())
                .fechaHoraInicio(reserva.getFechaHoraInicio())
                .fechaHoraFin(reserva.getFechaHoraFin())
                .estado(reserva.getEstado())
                .motivoCancelacion(reserva.getMotivoCancelacion())
                .usuarioId(reserva.getUsuario() != null ? reserva.getUsuario().getId() : null)
                .vehiculoId(reserva.getVehiculo() != null ? reserva.getVehiculo().getId() : null)
                .espacioId(reserva.getEspacio() != null ? reserva.getEspacio().getId() : null);

        // Incluir información del QR si existe
        if (reserva.getEscaneoQR() != null) {
            builder.qrToken(reserva.getEscaneoQR().getToken())
                   .qrTipo(reserva.getEscaneoQR().getTipo())
                   .qrFechaExpiracion(reserva.getEscaneoQR().getFechaExpiracion())
                   .qrFechaInicioValidez(reserva.getEscaneoQR().getFechaInicioValidez());
        }

        return builder.build();
    }

    public static Reserva toEntity(ReservaDto reservaDto) {
        if (reservaDto == null) {
            return null;
        }

        return Reserva.builder()
                .id(reservaDto.getId())
                .fechaHoraInicio(reservaDto.getFechaHoraInicio())
                .fechaHoraFin(reservaDto.getFechaHoraFin())
                .estado(reservaDto.getEstado())
                .motivoCancelacion(reservaDto.getMotivoCancelacion())
                .build();
    }
}
