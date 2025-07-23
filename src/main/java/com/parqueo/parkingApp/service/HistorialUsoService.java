package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.HistorialUsoDto;
import java.time.LocalDateTime;
import java.util.List;
import com.parqueo.parkingApp.model.Usuario;
import com.parqueo.parkingApp.model.HistorialUso;

public interface HistorialUsoService {
    List<HistorialUsoDto> obtenerTodos();
    HistorialUsoDto obtenerPorId(Long id);
    HistorialUsoDto crear(HistorialUsoDto dto);
    HistorialUsoDto actualizar(Long id, HistorialUsoDto dto);
    void eliminar(Long id);
    List<HistorialUsoDto> buscarPorUsuario(Long usuarioId);
    List<HistorialUsoDto> buscarPorVehiculo(Long vehiculoId);
    List<HistorialUsoDto> buscarPorEspacio(Long espacioId);
    List<HistorialUsoDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<HistorialUsoDto> buscarUsosActivos();
    void registrarEvento(Usuario usuario, HistorialUso.AccionHistorial accion);
    void registrarEvento(Usuario usuario, com.parqueo.parkingApp.model.EspacioDisponible espacio, com.parqueo.parkingApp.model.Reserva reserva, com.parqueo.parkingApp.model.Vehiculo vehiculo, HistorialUso.AccionHistorial accion);
    List<HistorialUsoDto> obtenerMiHistorial();
    void eliminarPorReserva(Long reservaId);
}