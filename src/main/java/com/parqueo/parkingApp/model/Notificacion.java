package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones", indexes = {
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_fecha_creacion", columnList = "fecha_creacion"),
    @Index(name = "idx_leida", columnList = "leida")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @NotNull(message = "El título es obligatorio")
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @NotNull(message = "El mensaje es obligatorio")
    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    @NotNull(message = "La fecha de creación es obligatoria")
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "datos_adicionales", length = 1000)
    private String datosAdicionales; // JSON para datos adicionales

    public enum TipoNotificacion {
        SANCION,
        DESBLOQUEO,
        RESERVA_EXPIRADA,
        RESERVA_CREADA,
        ENTRADA_REGISTRADA,
        SALIDA_REGISTRADA,
        SISTEMA
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (leida == null) {
            leida = false;
        }
    }

    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", titulo='" + titulo + '\'' +
                ", tipo='" + tipo + '\'' +
                ", leida=" + leida +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
} 