package com.parqueo.parkingApp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parqueo.parkingApp.model.EspacioDisponible.EstadoEspacio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioDisponibleDto {

    private Long id;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @NotBlank(message = "El número de espacio es obligatorio")
    private String numeroEspacio;

    @NotBlank(message = "La zona es obligatoria")
    private String zona;

    @NotNull(message = "El estado del espacio es obligatorio")
    private EstadoEspacio estado;

    private Integer capacidadMaxima;

    private Boolean activo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaRegistro;
}
