package com.parqueo.parkingApp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.model.Reserva;

public interface EscaneoQRRepository extends JpaRepository<EscaneoQR, Long> {
    @Query("SELECT e FROM EscaneoQR e WHERE e.reserva = :reserva")
    Optional<EscaneoQR> findByReserva(@Param("reserva") Reserva reserva);
    List<EscaneoQR> findByReservaUsuarioId(Long usuarioId);
    List<EscaneoQR> findByTimestampEntBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    Optional<EscaneoQR> findByTokenAndTipo(String token, String tipo);
}
