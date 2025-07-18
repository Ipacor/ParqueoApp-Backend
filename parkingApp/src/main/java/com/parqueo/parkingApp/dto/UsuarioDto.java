package com.parqueo.parkingApp.dto;

import java.util.Set;

import com.parqueo.parkingApp.model.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {

    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @Email(message = "El email no tiene un formato válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private Long rolId;
    private String rolNombre;

    private Boolean activo;

    private java.time.LocalDateTime fechaRegistro;
    private java.time.LocalDateTime ultimoAcceso;

    // Relaciones simplificadas (solo IDs)
    private Set<Long> vehiculoIds;
    private Set<Long> reservaIds;
    private Set<Long> sancionIds;
    private Set<Long> historialUsoIds;

    private Set<String> permisos;
}
