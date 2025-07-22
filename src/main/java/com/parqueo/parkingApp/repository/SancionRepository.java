package com.parqueo.parkingApp.repository;

import com.parqueo.parkingApp.model.Sancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
