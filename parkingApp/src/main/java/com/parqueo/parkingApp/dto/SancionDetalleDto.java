package com.parqueo.parkingApp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SancionDetalleDto {
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La fecha de sanción es obligatoria")
    private LocalDateTime fechaSancion;

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    @NotNull(message = "El ID de la sanción es obligatorio")
    private Long sancionId;

    @NotNull(message = "El ID de la regla es obligatorio")
    private Long reglaId;
}