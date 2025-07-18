package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.SancionDetalleDto;

import java.util.List;

public interface SancionDetalleService {
    List<SancionDetalleDto> obtenerTodos();
    SancionDetalleDto obtenerPorId(Long id);
    SancionDetalleDto crear(SancionDetalleDto dto);
    SancionDetalleDto actualizar(Long id, SancionDetalleDto dto);
    void eliminar(Long id);
    List<SancionDetalleDto> buscarPorSancion(Long sancionId);
}
