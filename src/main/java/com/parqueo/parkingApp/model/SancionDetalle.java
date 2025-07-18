package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sancion_detalle", indexes = {
    @Index(name = "idx_sancion_id", columnList = "sancion_id"),
    @Index(name = "idx_regla_id", columnList = "regla_id"),
    @Index(name = "idx_nivel_gravedad", columnList = "nivel_gravedad")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SancionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotNull(message = "La fecha de sanción es obligatoria")
    @Column(name = "fecha_sancion", nullable = false)
    private LocalDateTime fechaSancion;

    public enum EstadoDetalle {
        ACTIVO,
        RESUELTO,
        ANULADO
    }

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoDetalle estado;

    @NotNull(message = "La sanción es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sancion_id", nullable = false)
    @ToString.Exclude
    private Sancion sancion;

    @NotNull(message = "La regla es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regla_id", nullable = false)
    @ToString.Exclude
    private ReglasEstacionamiento regla;

    public SancionDetalle(String descripcion, LocalDateTime fechaSancion, EstadoDetalle estado, Sancion sancion, ReglasEstacionamiento regla) {
        this.descripcion = descripcion;
        this.fechaSancion = fechaSancion;
        this.estado = estado;
        this.sancion = sancion;
        this.regla = regla;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaSancion == null) {
            fechaSancion = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "SancionDetalle{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", fechaSancion=" + fechaSancion +
                ", estado=" + estado +
                ", sancion=" + (sancion != null ? sancion.getId() : null) +
                ", regla=" + (regla != null ? regla.getId() : null) +
                '}';
    }
} 