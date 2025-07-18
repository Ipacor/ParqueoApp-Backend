package com.parqueo.parkingApp.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscaneoQRDto {
    private Long id;
    private Long reservaId;
    private LocalDateTime timestampEnt;
    private LocalDateTime timestampSal;
    private String token;
    private String tipo;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaInicioValidez;
}
