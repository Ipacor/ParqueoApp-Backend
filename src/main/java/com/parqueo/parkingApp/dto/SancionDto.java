package com.parqueo.parkingApp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parqueo.parkingApp.model.Sancion.EstadoSancion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SancionDto {
    private Long id;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID del vehículo es obligatorio")
    private Long vehiculoId;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @NotNull(message = "El estado de la sanción es obligatorio")
    private EstadoSancion estado;

    @NotNull(message = "La fecha de registro es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime registroSancion;

    private String observaciones;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaResolucion;
    
    private String registradaPorNombre;

    // Nuevo campo para el id de la regla infringida
    private Long reglaId;

    private String tipoCastigo;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaInicioSuspension;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaFinSuspension;
}