package com.parqueo.parkingApp.dto;

import com.parqueo.parkingApp.model.Vehiculo;
import com.parqueo.parkingApp.model.Vehiculo.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoDto {

    private Long id;

    @NotBlank(message = "La placa es obligatoria")
    private String placa;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El color es obligatorio")
    private String color;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipo;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    private Boolean activo;

    private java.time.LocalDateTime fechaRegistro;
}
