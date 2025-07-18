package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.ReglasEstacionamientoDto;
import java.util.List;

public interface ReglasEstacionamientoService {
    List<ReglasEstacionamientoDto> obtenerTodos();
    ReglasEstacionamientoDto obtenerPorId(Long id);
    ReglasEstacionamientoDto crear(ReglasEstacionamientoDto dto);
    ReglasEstacionamientoDto actualizar(Long id, ReglasEstacionamientoDto dto);
    void eliminar(Long id);
    List<ReglasEstacionamientoDto> buscarPorTipo(String tipo);
    List<ReglasEstacionamientoDto> buscarActivas();
}