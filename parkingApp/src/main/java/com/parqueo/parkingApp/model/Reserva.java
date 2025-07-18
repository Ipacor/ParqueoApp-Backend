package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reservas", indexes = {
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_vehiculo_id", columnList = "vehiculo_id"),
    @Index(name = "idx_espacio_id", columnList = "espacio_id"),
    @Index(name = "idx_fecha_inicio", columnList = "fecha_hora_inicio"),
    @Index(name = "idx_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_id", nullable = true)
    @ToString.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "vehiculo_id", nullable = true)
    @ToString.Exclude
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "espacio_id", nullable = true)
    @ToString.Exclude
    private EspacioDisponible espacio;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private EscaneoQR escaneoQR;

    @OneToMany(mappedBy = "reserva")
    @ToString.Exclude
    private Set<HistorialUso> historialUso = new HashSet<>();

    public enum EstadoReserva {
        RESERVADO,
        ACTIVO,
        FINALIZADO,
        CANCELADO,
        EXPIRADO
    }

    public Reserva(Usuario usuario, Vehiculo vehiculo, EspacioDisponible espacio, 
                   LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.espacio = espacio;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoReserva.RESERVADO;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoReserva.RESERVADO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", vehiculo=" + (vehiculo != null ? vehiculo.getId() : null) +
                ", espacio=" + (espacio != null ? espacio.getId() : null) +
                ", fechaHoraInicio=" + fechaHoraInicio +
                ", fechaHoraFin=" + fechaHoraFin +
                ", estado=" + estado +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}