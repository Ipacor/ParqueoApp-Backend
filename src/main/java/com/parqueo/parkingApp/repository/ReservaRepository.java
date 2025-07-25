package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioId(Long usuarioId);
    List<Reserva> findByVehiculoId(Long vehiculoId);
    List<Reserva> findByEspacioId(Long espacioId);
    List<Reserva> findByEstado(Reserva.EstadoReserva estado);
    List<Reserva> findByFechaHoraInicioBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    @Query("SELECT r FROM Reserva r WHERE r.estado = :estado AND r.fechaHoraInicio >= :fechaActual")
    List<Reserva> findReservasActivas(@Param("estado") Reserva.EstadoReserva estado, @Param("fechaActual") LocalDateTime fechaActual);
    
    @Query("SELECT r FROM Reserva r LEFT JOIN FETCH r.espacio LEFT JOIN FETCH r.usuario LEFT JOIN FETCH r.vehiculo WHERE r.estado = :estado")
    List<Reserva> findByEstadoWithRelations(@Param("estado") Reserva.EstadoReserva estado);
    
    // Métodos para estadísticas del dashboard
    long countByEstado(Reserva.EstadoReserva estado);
    
    @Query("SELECT COUNT(r) FROM Reserva r WHERE CAST(r.fechaHoraInicio AS date) = :fecha")
    long countByFechaReserva(@Param("fecha") java.time.LocalDate fecha);
    
    @Query("SELECT COUNT(r) FROM Reserva r WHERE CAST(r.fechaHoraInicio AS date) BETWEEN :fechaInicio AND :fechaFin")
    long countByFechaReservaBetween(@Param("fechaInicio") java.time.LocalDate fechaInicio, @Param("fechaFin") java.time.LocalDate fechaFin);
    
    // Método para encontrar reservas conflictivas (que se solapan)
    @Query("SELECT r1 FROM Reserva r1, Reserva r2 WHERE r1.id != r2.id AND r1.espacio = r2.espacio " +
           "AND r1.estado IN ('RESERVADO', 'ACTIVO') AND r2.estado IN ('RESERVADO', 'ACTIVO') " +
           "AND ((r1.fechaHoraInicio BETWEEN r2.fechaHoraInicio AND r2.fechaHoraFin) " +
           "OR (r1.fechaHoraFin BETWEEN r2.fechaHoraInicio AND r2.fechaHoraFin) " +
           "OR (r2.fechaHoraInicio BETWEEN r1.fechaHoraInicio AND r1.fechaHoraFin))")
    List<Reserva> findReservasConflictivas();
}
