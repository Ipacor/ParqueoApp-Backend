package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sanciones", indexes = {
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_vehiculo_id", columnList = "vehiculo_id"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_fecha_registro", columnList = "registro_sancion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @NotNull(message = "El vehículo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    @ToString.Exclude
    private Vehiculo vehiculo;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    @Column(nullable = false, length = 500)
    private String motivo;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSancion estado;

    @NotNull(message = "La fecha de registro es obligatoria")
    @Column(name = "registro_sancion", nullable = false)
    private LocalDateTime registroSancion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @OneToMany(mappedBy = "sancion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<SancionDetalle> detalles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrada_por_id")
    private Usuario registradaPor;

    @Column(name = "tipo_castigo", length = 50)
    private String tipoCastigo;

    @Column(name = "fecha_inicio_suspension")
    private LocalDateTime fechaInicioSuspension;

    @Column(name = "fecha_fin_suspension")
    private LocalDateTime fechaFinSuspension;

    // Enum interno
    public enum EstadoSancion {
        ACTIVA,
        RESUELTA,
        ANULADA
    }

    // Getters & Setters (actualiza tipo de estado)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoSancion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSancion estado) {
        this.estado = estado;
    }

    public LocalDateTime getRegistroSancion() {
        return registroSancion;
    }

    public void setRegistroSancion(LocalDateTime registroSancion) {
        this.registroSancion = registroSancion;
    }

    public Set<SancionDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(Set<SancionDetalle> detalles) {
        this.detalles = detalles;
    }

    public String getTipoCastigo() {
        return tipoCastigo;
    }

    public void setTipoCastigo(String tipoCastigo) {
        this.tipoCastigo = tipoCastigo;
    }

    public LocalDateTime getFechaInicioSuspension() {
        return fechaInicioSuspension;
    }

    public void setFechaInicioSuspension(LocalDateTime fechaInicioSuspension) {
        this.fechaInicioSuspension = fechaInicioSuspension;
    }

    public LocalDateTime getFechaFinSuspension() {
        return fechaFinSuspension;
    }

    public void setFechaFinSuspension(LocalDateTime fechaFinSuspension) {
        this.fechaFinSuspension = fechaFinSuspension;
    }

    // Constructores
    public Sancion(Usuario usuario, Vehiculo vehiculo, String motivo) {
        this.usuario = usuario;
        this.vehiculo = vehiculo;
        this.motivo = motivo;
        this.registroSancion = LocalDateTime.now();
        this.estado = EstadoSancion.ACTIVA;
    }

    @PrePersist
    protected void onCreate() {
        if (registroSancion == null) {
            registroSancion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSancion.ACTIVA;
        }
    }

    @Override
    public String toString() {
        return "Sancion{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getId() : null) +
                ", vehiculo=" + (vehiculo != null ? vehiculo.getId() : null) +
                ", motivo='" + motivo + '\'' +
                ", estado=" + estado +
                ", registroSancion=" + registroSancion +
                ", tipoCastigo='" + tipoCastigo + '\'' +
                ", fechaInicioSuspension=" + fechaInicioSuspension +
                ", fechaFinSuspension=" + fechaFinSuspension +
                '}';
    }
}
