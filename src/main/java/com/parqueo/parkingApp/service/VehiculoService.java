package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.VehiculoDto;
import com.parqueo.parkingApp.model.Vehiculo;
import java.util.List;
import java.util.Optional;

public interface VehiculoService {
    List<VehiculoDto> obtenerTodos();
    VehiculoDto obtenerPorId(Long id);
    VehiculoDto crear(VehiculoDto dto);
    VehiculoDto actualizar(Long id, VehiculoDto dto);
    void eliminar(Long id);
    Optional<VehiculoDto> buscarPorPlaca(String placa);
    List<VehiculoDto> buscarPorUsuario(Long usuarioId);
    List<VehiculoDto> buscarPorTipo(Vehiculo.TipoVehiculo tipo);
}

