package com.parqueo.parkingApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistorialUsoDto {
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID del espacio es obligatorio")
    private Long espacioId;

    @NotNull(message = "La fecha de uso es obligatoria")
    private LocalDateTime fechaUso;

    @NotBlank(message = "La acción es obligatoria")
    private String accion;

    private String usuarioNombre;

    private String vehiculoPlaca;

    private String notas;
}