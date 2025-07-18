package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.ReservaDto;
import com.parqueo.parkingApp.model.Reserva;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaService {
    List<ReservaDto> obtenerTodos();
    ReservaDto obtenerPorId(Long id);
    ReservaDto crear(ReservaDto dto);
    ReservaDto actualizar(Long id, ReservaDto dto);
    void eliminar(Long id);
    List<ReservaDto> buscarPorUsuario(Long usuarioId);
    List<ReservaDto> buscarPorVehiculo(Long vehiculoId);
    List<ReservaDto> buscarPorEspacio(Long espacioId);
    List<ReservaDto> buscarPorEstado(Reserva.EstadoReserva estado);
    List<ReservaDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<ReservaDto> buscarReservasActivas();
    void liberarEspaciosReservadosExpirados();
}

