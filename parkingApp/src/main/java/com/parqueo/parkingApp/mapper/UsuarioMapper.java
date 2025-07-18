package com.parqueo.parkingApp.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.parqueo.parkingApp.dto.UsuarioDto;
import com.parqueo.parkingApp.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public static UsuarioDto toDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioDto.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .fechaRegistro(usuario.getFechaRegistro())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .rolId(usuario.getRol() != null ? usuario.getRol().getId() : null)
                .rolNombre(usuario.getRol() != null ? usuario.getRol().getNombre() : null)
                .vehiculoIds(mapVehiculoIds(usuario))
                .reservaIds(mapReservaIds(usuario))
                .sancionIds(mapSancionIds(usuario))
                .historialUsoIds(mapHistorialUsoIds(usuario))
                .permisos(mapPermisos(usuario))
                .build();
    }

    public static Usuario toEntity(UsuarioDto usuarioDto) {
        if (usuarioDto == null) {
            return null;
        }

        return Usuario.builder()
                .id(usuarioDto.getId())
                .username(usuarioDto.getUsername())
                .password(usuarioDto.getPassword())
                .nombreCompleto(usuarioDto.getNombreCompleto())
                .email(usuarioDto.getEmail())
                .fechaRegistro(usuarioDto.getFechaRegistro())
                .ultimoAcceso(usuarioDto.getUltimoAcceso())
                .activo(usuarioDto.getActivo())
                .build();
    }

    private static Set<Long> mapVehiculoIds(Usuario usuario) {
        if (usuario == null || usuario.getVehiculos() == null) {
            return null;
        }
        return usuario.getVehiculos().stream()
                .map(vehiculo -> vehiculo.getId())
                .collect(Collectors.toSet());
    }

    private static Set<Long> mapReservaIds(Usuario usuario) {
        if (usuario == null || usuario.getReservas() == null) {
            return null;
        }
        return usuario.getReservas().stream()
                .map(reserva -> reserva.getId())
                .collect(Collectors.toSet());
    }

    private static Set<Long> mapSancionIds(Usuario usuario) {
        if (usuario == null || usuario.getSanciones() == null) {
            return null;
        }
        return usuario.getSanciones().stream()
                .map(sancion -> sancion.getId())
                .collect(Collectors.toSet());
    }

    private static Set<Long> mapHistorialUsoIds(Usuario usuario) {
        if (usuario == null || usuario.getHistorialUso() == null) {
            return null;
        }
        return usuario.getHistorialUso().stream()
                .map(historial -> historial.getId())
                .collect(Collectors.toSet());
    }

    private static Set<String> mapPermisos(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getPermisos() == null) {
            return null;
        }
        return usuario.getRol().getPermisos().stream()
                .map(permiso -> permiso.getNombre())
                .collect(Collectors.toSet());
    }
}
