package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.EscaneoQR;
import com.parqueo.parkingApp.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EscaneoQRRepository extends JpaRepository<EscaneoQR, Long> {

    Optional<EscaneoQR> findByReserva(Reserva reserva);

    List<EscaneoQR> findByReservaUsuarioId(Long usuarioId);

    List<EscaneoQR> findByTimestampEntBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    Optional<EscaneoQR> findByTokenAndTipo(String token, String tipo);

    @Query("SELECT e FROM EscaneoQR e LEFT JOIN FETCH e.reserva r LEFT JOIN FETCH r.espacio LEFT JOIN FETCH r.usuario LEFT JOIN FETCH r.vehiculo")
    List<EscaneoQR> findAllWithReservaAndEspacio();
}
