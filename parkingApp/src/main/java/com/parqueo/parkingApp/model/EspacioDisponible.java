package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "espacios_disponibles", indexes = {
    @Index(name = "idx_ubicacion", columnList = "ubicacion"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_zona", columnList = "zona")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioDisponible {

    public enum EstadoEspacio {
        DISPONIBLE,
        OCUPADO,
        RESERVADO,
        FUERA_DE_SERVICIO,
        MANTENIMIENTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(min = 2, max = 100, message = "La ubicación debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String ubicacion;

    @NotBlank(message = "El número de espacio es obligatorio")
    @Size(min = 1, max = 10, message = "El número de espacio debe tener entre 1 y 10 caracteres")
    @Column(nullable = false, length = 10)
    private String numeroEspacio;

    @NotBlank(message = "La zona es obligatoria")
    @Size(min = 1, max = 50, message = "La zona debe tener entre 1 y 50 caracteres")
    @Column(nullable = false, length = 50)
    private String zona;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEspacio estado;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima = 1;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "espacio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "espacio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<HistorialUso> historialUso = new HashSet<>();

    public EspacioDisponible(String ubicacion, String numeroEspacio, String zona) {
        this.ubicacion = ubicacion;
        this.numeroEspacio = numeroEspacio;
        this.zona = zona;
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.estado = EstadoEspacio.DISPONIBLE;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (estado == null) {
            estado = EstadoEspacio.DISPONIBLE;
        }
        if (capacidadMaxima == null) {
            capacidadMaxima = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaActualizacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "EspacioDisponible{" +
                "id=" + id +
                ", ubicacion='" + ubicacion + '\'' +
                ", numeroEspacio='" + numeroEspacio + '\'' +
                ", zona='" + zona + '\'' +
                ", estado=" + estado +
                ", capacidadMaxima=" + capacidadMaxima +
                ", fechaRegistro=" + fechaRegistro +
                ", activo=" + activo +
                '}';
    }
}
