package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.SancionDto;
import com.parqueo.parkingApp.model.Sancion;
import java.time.LocalDateTime;
import java.util.List;

public interface SancionService {
    List<SancionDto> obtenerTodos();
    SancionDto obtenerPorId(Long id);
    SancionDto crear(SancionDto dto);
    SancionDto actualizar(Long id, SancionDto dto);
    void eliminar(Long id);
    List<SancionDto> buscarPorUsuario(Long usuarioId);
    List<SancionDto> buscarPorVehiculo(Long vehiculoId);
    List<SancionDto> buscarPorEstado(Sancion.EstadoSancion estado);
    List<SancionDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<SancionDto> buscarSancionesActivas();
    Sancion guardarEntidad(Sancion sancion);
    SancionDto crearConRegistrador(SancionDto dto, com.parqueo.parkingApp.model.Usuario registradaPor);
}
