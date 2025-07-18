package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "escaneo_qr", indexes = {
    @Index(name = "idx_reserva_id", columnList = "reserva_id"),
    @Index(name = "idx_timestamp_ent", columnList = "timestamp_ent")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscaneoQR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La reserva es obligatoria")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    @ToString.Exclude
    private Reserva reserva;

    @Column(name = "timestamp_ent")
    private LocalDateTime timestampEnt;

    @Column(name = "timestamp_sal")
    private LocalDateTime timestampSal;

    @Column(name = "token", unique = true, nullable = false, length = 128)
    private String token;

    @Column(name = "tipo", nullable = false, length = 16)
    private String tipo; // ENTRADA o SALIDA

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_inicio_validez")
    private LocalDateTime fechaInicioValidez;

    public EscaneoQR(Reserva reserva, LocalDateTime timestampEnt, LocalDateTime timestampSal) {
        this.reserva = reserva;
        this.timestampEnt = timestampEnt;
        this.timestampSal = timestampSal;
    }

    @Override
    public String toString() {
        return "EscaneoQR{" +
                "id=" + id +
                ", reserva=" + (reserva != null ? reserva.getId() : null) +
                ", timestampEnt=" + timestampEnt +
                ", timestampSal=" + timestampSal +
                '}';
    }
}
