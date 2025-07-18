package com.parqueo.parkingApp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "vehiculos", indexes = {
    @Index(name = "idx_placa", columnList = "placa"),
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{3}$", message = "La placa debe tener el formato AAA000")
    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(min = 2, max = 50, message = "El modelo debe tener entre 2 y 50 caracteres")
    @Column(nullable = false, length = 50)
    private String modelo;

    @NotBlank(message = "La marca es obligatoria")
    @Size(min = 2, max = 50, message = "La marca debe tener entre 2 y 50 caracteres")
    @Column(nullable = false, length = 50)
    private String marca;

    @NotBlank(message = "El color es obligatorio")
    @Size(min = 2, max = 30, message = "El color debe tener entre 2 y 30 caracteres")
    @Column(nullable = false, length = 30)
    private String color;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVehiculo tipo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Sancion> sanciones = new HashSet<>();

    public enum TipoVehiculo {
        AUTOMOVIL,
        MOTOCICLETA,
        CAMIONETA,
        CAMION,
        BUS
    }

    public Vehiculo(String placa, String modelo, String marca, String color, TipoVehiculo tipo, Usuario usuario) {
        this.placa = placa;
        this.modelo = modelo;
        this.marca = marca;
        this.color = color;
        this.tipo = tipo;
        this.usuario = usuario;
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }

    @Override
    public String toString() {
        return "Vehiculo{" +
                "id=" + id +
                ", placa='" + placa + '\'' +
                ", modelo='" + modelo + '\'' +
                ", marca='" + marca + '\'' +
                ", color='" + color + '\'' +
                ", tipo=" + tipo +
                ", fechaRegistro=" + fechaRegistro +
                ", activo=" + activo +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                '}';
    }

}
