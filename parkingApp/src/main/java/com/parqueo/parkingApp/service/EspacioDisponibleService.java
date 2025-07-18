package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.EspacioDisponibleDto;
import com.parqueo.parkingApp.model.EspacioDisponible;

import java.util.List;

public interface EspacioDisponibleService {
    List<EspacioDisponibleDto> obtenerTodos();
    EspacioDisponibleDto obtenerPorId(Long id);
    EspacioDisponibleDto crear(EspacioDisponibleDto dto);
    EspacioDisponibleDto actualizar(Long id, EspacioDisponibleDto dto);
    void eliminar(Long id);
    List<EspacioDisponibleDto> buscarPorEstado(EspacioDisponible.EstadoEspacio estado);
    List<EspacioDisponibleDto> buscarPorZona(String zona);
    List<EspacioDisponibleDto> buscarDisponibles();
}
