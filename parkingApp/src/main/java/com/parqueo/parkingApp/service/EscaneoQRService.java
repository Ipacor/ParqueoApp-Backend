package com.parqueo.parkingApp.service;

import com.parqueo.parkingApp.dto.EscaneoQRDto;
import java.time.LocalDateTime;
import java.util.List;

public interface EscaneoQRService {
    List<EscaneoQRDto> obtenerTodos();
    EscaneoQRDto obtenerPorId(Long id);
    EscaneoQRDto crear(EscaneoQRDto dto);
    EscaneoQRDto actualizar(Long id, EscaneoQRDto dto);
    void eliminar(Long id);
    EscaneoQRDto registrarEntrada(Long reservaId);
    EscaneoQRDto registrarSalida(Long reservaId);
    EscaneoQRDto obtenerPorReserva(Long reservaId);
    List<EscaneoQRDto> buscarPorUsuario(Long usuarioId);
    List<EscaneoQRDto> buscarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
