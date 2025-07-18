package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_uso", indexes = {
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_espacio_id", columnList = "espacio_id"),
    @Index(name = "idx_fecha_uso", columnList = "fecha_uso")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialUso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = true)
    @ToString.Exclude
    private EspacioDisponible espacio;

    @NotNull(message = "La fecha de uso es obligatoria")
    @Column(name = "fecha_uso", nullable = false)
    private LocalDateTime fechaUso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    @ToString.Exclude
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    @ToString.Exclude
    private Vehiculo vehiculo;

    public enum AccionHistorial {
        ENTRADA,
        SALIDA,
        RESERVA,
        CANCELACION,
        SANCION,
        DESBLOQUEO,
        OTRO
    }

    @NotNull(message = "La acción es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccionHistorial accion;

    @Column(length = 255)
    private String notas;

    public HistorialUso(Usuario usuario, EspacioDisponible espacio, LocalDateTime fechaUso, Reserva reserva, Vehiculo vehiculo, AccionHistorial accion) {
        this.usuario = usuario;
        this.espacio = espacio;
        this.fechaUso = fechaUso;
        this.reserva = reserva;
        this.vehiculo = vehiculo;
        this.accion = accion;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaUso == null) {
            fechaUso = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "HistorialUso{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", espacio=" + (espacio != null ? espacio.getId() : null) +
                ", fechaUso=" + fechaUso +
                ", reserva=" + (reserva != null ? reserva.getId() : null) +
                ", vehiculo=" + (vehiculo != null ? vehiculo.getId() : null) +
                ", accion='" + accion + '\'' +
                '}';
    }
}