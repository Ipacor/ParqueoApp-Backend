package com.parqueo.parkingApp.dto;

import com.parqueo.parkingApp.model.Reserva.EstadoReserva;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDto {

    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID del vehículo es obligatorio")
    private Long vehiculoId;

    @NotNull(message = "El ID del espacio es obligatorio")
    private Long espacioId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe estar en el futuro")
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe estar en el futuro")
    private LocalDateTime fechaHoraFin;

    @NotNull(message = "El estado es obligatorio")
    private EstadoReserva estado;

    private String motivoCancelacion;
}
