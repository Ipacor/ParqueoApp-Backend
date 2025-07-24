package com.parqueo.parkingApp.mapper;

import java.util.HashSet;
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

        try {
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
        } catch (Exception e) {
            System.out.println("Error en UsuarioMapper.toDto: " + e.getMessage());
            e.printStackTrace();
            // Retornar un DTO básico en caso de error
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
                    .build();
        }
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
        try {
            if (usuario == null || usuario.getVehiculos() == null) {
                return new HashSet<>();
            }
            return usuario.getVehiculos().stream()
                    .map(vehiculo -> vehiculo.getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Error mapeando vehiculoIds: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static Set<Long> mapReservaIds(Usuario usuario) {
        try {
            if (usuario == null || usuario.getReservas() == null) {
                return new HashSet<>();
            }
            return usuario.getReservas().stream()
                    .map(reserva -> reserva.getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Error mapeando reservaIds: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static Set<Long> mapSancionIds(Usuario usuario) {
        try {
            if (usuario == null || usuario.getSanciones() == null) {
                return new HashSet<>();
            }
            return usuario.getSanciones().stream()
                    .map(sancion -> sancion.getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Error mapeando sancionIds: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static Set<Long> mapHistorialUsoIds(Usuario usuario) {
        try {
            if (usuario == null || usuario.getHistorialUso() == null) {
                return new HashSet<>();
            }
            return usuario.getHistorialUso().stream()
                    .map(historial -> historial.getId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Error mapeando historialUsoIds: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static Set<String> mapPermisos(Usuario usuario) {
        try {
            if (usuario == null || usuario.getRol() == null || usuario.getRol().getPermisos() == null) {
                return new HashSet<>();
            }
            return usuario.getRol().getPermisos().stream()
                    .map(permiso -> permiso.getNombre())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Error mapeando permisos: " + e.getMessage());
            return new HashSet<>();
        }
    }
}
