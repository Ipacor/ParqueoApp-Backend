package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {
    List<Sancion> findByUsuarioId(Long usuarioId); // Para buscar por usuario
    List<Sancion> findByVehiculoId(Long vehiculoId);
    List<Sancion> findByEstado(Sancion.EstadoSancion estado);
    List<Sancion> findByRegistroSancionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT s FROM Sancion s LEFT JOIN FETCH s.registradaPor")
    List<Sancion> findAllWithRegistrador();
    
    @Query("SELECT COUNT(s) FROM Sancion s LEFT JOIN s.registradaPor")
    Long countAllWithRegistrador();
    
    // Métodos para estadísticas del dashboard
    long countByEstado(Sancion.EstadoSancion estado);
    
    @Query("SELECT COUNT(s) FROM Sancion s WHERE s.fechaSancion BETWEEN :fechaInicio AND :fechaFin")
    long countByFechaSancionBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
}
