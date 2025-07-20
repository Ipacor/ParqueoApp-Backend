package com.parqueo.parkingApp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscaneoQRDto {
    private Long id;
    private Long reservaId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime timestampEnt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime timestampSal;
    
    private String token;
    private String tipo;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaExpiracion;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Lima")
    private LocalDateTime fechaInicioValidez;
}
