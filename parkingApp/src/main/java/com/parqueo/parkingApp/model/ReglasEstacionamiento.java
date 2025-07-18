package com.parqueo.parkingApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "reglas_estacionamiento", indexes = {
    @Index(name = "idx_tipo_sancion", columnList = "tipo_sancion"),
    @Index(name = "idx_descripcion", columnList = "descripcion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglasEstacionamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String descripcion;

    @NotNull(message = "El tipo de falta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_falta", nullable = false, length = 10)
    private TipoFalta tipoFalta;

    public enum TipoFalta {
        LEVE,
        GRAVE
    }

    public ReglasEstacionamiento(String descripcion, TipoFalta tipoFalta) {
        this.descripcion = descripcion;
        this.tipoFalta = tipoFalta;
    }

    @Override
    public String toString() {
        return "ReglasEstacionamiento{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", tipoFalta=" + tipoFalta +
                '}';
    }
}