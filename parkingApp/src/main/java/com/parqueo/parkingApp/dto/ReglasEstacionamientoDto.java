package com.parqueo.parkingApp.dto;

import com.parqueo.parkingApp.model.ReglasEstacionamiento.TipoFalta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglasEstacionamientoDto {
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "El tipo de falta es obligatorio")
    private TipoFalta tipoFalta;
} 